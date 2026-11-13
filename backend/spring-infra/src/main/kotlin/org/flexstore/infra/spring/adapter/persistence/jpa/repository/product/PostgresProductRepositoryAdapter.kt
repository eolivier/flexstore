package org.flexstore.infra.spring.adapter.persistence.jpa.repository.product

import org.flexstore.domain.product.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductTranslationEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.product.*
import org.flexstore.infra.spring.adapter.mapping.endsAt
import org.flexstore.infra.spring.adapter.mapping.percentage
import org.flexstore.infra.spring.adapter.mapping.startsAt
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.config.PRODUCTS_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// Catalog order: oldest products first, then by id
private val CATALOG_ORDER = Sort.by("createdAt", "id")

// The reads of the product table go through one cache: the catalog, a product by id and the products of a
// category are each cached under their own key. A save (insert or update) evicts them all, so that the next read
// loads the table again. Not cached: the existence checks
@Repository
@Transactional
class PostgresProductRepositoryAdapter(
    private val jpaRepo: JpaProductRepository
) : ProductRepository {

    @CacheEvict(PRODUCTS_CACHE, allEntries = true)
    override fun save(product: Product): Product {
        val entity = when (product) {
            is DraftProduct -> product.toEntity(Identity.nextId().value)
            is DefinedProduct -> product.toEntity()
        }
        return jpaRepo.save(entity).toDomain()
    }

    @Cacheable(PRODUCTS_CACHE, key = "'all'")
    @Transactional(readOnly = true)
    override fun findAll(): List<Product> = jpaRepo.findAll(CATALOG_ORDER).map { it.toDomain() }

    @Cacheable(PRODUCTS_CACHE, key = "'id:' + #p0.value")
    @Transactional(readOnly = true)
    override fun findById(productId: ProductId): DefinedProduct? =
        jpaRepo.findById(productId.value).map { it.toDomain() }.orElse(null)

    @Cacheable(PRODUCTS_CACHE, key = "'category:' + #p0.name")
    @Transactional(readOnly = true)
    override fun findAllBy(category: Category): List<Product> =
        jpaRepo.findAllByCategory(category.name, CATALOG_ORDER).map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun exists(product: Product): Boolean = when (product) {
        // Prices are compared by the database, whatever their scale (25 = 25.00)
        is DraftProduct -> jpaRepo.existsWith(
            Language.DEFAULT.name.lowercase(),
            product.name.value,
            product.description.value,
            product.category.name,
            product.price.amount.value,
            product.price.currency.name
        )
        is DefinedProduct -> jpaRepo.existsById(product.productId.value)
    }

    override fun notExists(product: Product): Boolean = !exists(product)

    @Transactional(readOnly = true)
    override fun productExists(productId: ProductId): Boolean = jpaRepo.existsById(productId.value)

    @Transactional(readOnly = true)
    override fun skuIsUsedByAnotherProduct(product: DefinedProduct): Boolean =
        jpaRepo.existsBySkuAndIdNot(product.sku.value, product.productId.value)

    @Transactional(readOnly = true)
    override fun skuIsFree(sku: Sku): Boolean = !jpaRepo.existsBySku(sku.value)

    private fun DraftProduct.toEntity(id: String) = ProductEntity(
        id = id,
        sku = sku.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name,
        vatRate = pricing.vatRate.name,
        promotionPercentage = pricing.promotion.percentage(),
        promotionStartsAt = pricing.promotion.startsAt(),
        promotionEndsAt = pricing.promotion.endsAt(),
        translations = translations.asList().map { it.toNewEntity() }
    )

    private fun DefinedProduct.toEntity() = ProductEntity(
        id = productId.value,
        sku = sku.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name,
        vatRate = vatRate.name,
        promotionPercentage = promotion.percentage(),
        promotionStartsAt = promotion.startsAt(),
        promotionEndsAt = promotion.endsAt(),
        translations = translationEntities(this)
    )

    // The stored row of a language keeps its id and its creation date, so that a product is translated at most
    // once per language; the other languages get new rows
    private fun translationEntities(product: DefinedProduct): List<ProductTranslationEntity> {
        val storedProduct = jpaRepo.findById(product.productId.value)
        val storedTranslations = storedProduct.map { it.translations }.orElse(emptyList())
        return product.translations.asList().map { translation ->
            val language = translation.language.name.lowercase()
            val stored = storedTranslations.firstOrNull { it.language == language }
            if (stored == null) {
                translation.toNewEntity()
            } else {
                val name = translation.name.value
                val entity = ProductTranslationEntity(stored.id, language, name, translation.description.value)
                entity.also { it.createdAt = stored.createdAt }
            }
        }
    }

    private fun ProductTranslation.toNewEntity(): ProductTranslationEntity {
        val id = Identity.nextId().value
        return ProductTranslationEntity(id, language.name.lowercase(), name.value, description.value)
    }
}
