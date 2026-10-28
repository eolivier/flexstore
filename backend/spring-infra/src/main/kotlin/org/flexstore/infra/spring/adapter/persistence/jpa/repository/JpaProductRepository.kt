package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductTranslationEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
interface JpaProductRepository : JpaRepository<ProductEntity, String> {
    fun findAllByCategory(category: String, sort: Sort): List<ProductEntity>
    fun existsByNameAndDescriptionAndCategoryAndPriceAndCurrency(
        name: String, description: String, category: String, price: BigDecimal, currency: String
    ): Boolean
}

// Catalog order: oldest products first, then by id
private val CATALOG_ORDER = Sort.by("createdAt", "id")

@Repository
@Transactional
class PostgresProductRepositoryAdapter(
    private val jpaRepo: JpaProductRepository
) : ProductRepository {

    override fun save(product: Product): Product {
        val entity = when (product) {
            is DraftProduct -> product.toEntity(Identity.nextId().value)
            is DefinedProduct -> product.toEntity()
        }
        return jpaRepo.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Product> = jpaRepo.findAll(CATALOG_ORDER).map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findById(productId: ProductId): DefinedProduct? =
        jpaRepo.findById(productId.id.value).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun findAllBy(category: Category): List<Product> =
        jpaRepo.findAllByCategory(category.name, CATALOG_ORDER).map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun exists(product: Product): Boolean = when (product) {
        // Prices are compared by the database, whatever their scale (25 = 25.00)
        is DraftProduct -> jpaRepo.existsByNameAndDescriptionAndCategoryAndPriceAndCurrency(
            product.name.value,
            product.description.value,
            product.category.name,
            product.price.amount.value,
            product.price.currency.name
        )
        is DefinedProduct -> jpaRepo.existsById(product.productId.id.value)
    }

    override fun notExists(product: Product): Boolean = !exists(product)

    @Transactional(readOnly = true)
    override fun productExists(productId: ProductId): Boolean = jpaRepo.existsById(productId.id.value)

    private fun DraftProduct.toEntity(id: String) = ProductEntity(
        id = id,
        name = name.value,
        description = description.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name,
        translations = translations.otherThanDefault().map { it.toNewEntity() }
    )

    private fun DefinedProduct.toEntity() = ProductEntity(
        id = productId.id.value,
        name = name.value,
        description = description.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name,
        translations = translationEntities(this)
    )

    // The stored row of a language keeps its id and its creation date, so that a product is translated at most
    // once per language; the other languages get new rows
    private fun translationEntities(product: DefinedProduct): List<ProductTranslationEntity> {
        val storedProduct = jpaRepo.findById(product.productId.id.value)
        val storedTranslations = storedProduct.map { it.translations }.orElse(emptyList())
        return product.translations.otherThanDefault().map { translation ->
            val language = translation.language.name.lowercase()
            val stored = storedTranslations.firstOrNull { it.language == language }
            if (stored != null) {
                val name = translation.name.value
                val entity = ProductTranslationEntity(stored.id, language, name, translation.description.value)
                entity.also { it.createdAt = stored.createdAt }
            } else {
                translation.toNewEntity()
            }
        }
    }

    private fun ProductTranslation.toNewEntity(): ProductTranslationEntity {
        val id = Identity.nextId().value
        return ProductTranslationEntity(id, language.name.lowercase(), name.value, description.value)
    }
}

// The name and the description of the product are its translation in the default language. A row of
// product_translation in the default language would repeat them: it is not read
fun ProductEntity.toDomain(): DefinedProduct {
    val defaultText = ProductText(Name(name), Description(description))
    val defaultTranslation = ProductTranslation(Language.DEFAULT, defaultText)
    val otherTranslations = translations.map { it.toDomain() }.filterNot { it.isIn(Language.DEFAULT) }
    val productTranslations = ProductTranslations(listOf(defaultTranslation) + otherTranslations)
    return DefinedProduct(
        ProductId(Identity(id)),
        productTranslations,
        Category.valueOf(category),
        Price(Amount(price), Currency.valueOf(currency))
    )
}

private fun ProductTranslationEntity.toDomain() =
    ProductTranslation(Language.valueOf(language.uppercase()), ProductText(Name(name), Description(description)))
