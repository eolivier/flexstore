package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

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
            is DraftProduct -> product.toEntity(UUID.randomUUID().toString())
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

    private fun DraftProduct.toEntity(id: String) = ProductEntity(
        id = id,
        name = name.value,
        description = description.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name
    )

    private fun DefinedProduct.toEntity() = ProductEntity(
        id = productId.id.value,
        name = name.value,
        description = description.value,
        category = category.name,
        price = price.amount.value,
        currency = price.currency.name
    )
}

fun ProductEntity.toDomain() = DefinedProduct(
    productId = ProductId(Identity(id)),
    name = Name(name),
    description = Description(description),
    category = Category.valueOf(category),
    price = Price(Amount(price), Currency.valueOf(currency))
)
