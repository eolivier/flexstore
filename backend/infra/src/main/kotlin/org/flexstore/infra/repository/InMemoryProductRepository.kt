package org.flexstore.infra.repository

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

class InMemoryProductRepository : ProductRepository {

    private val products: MutableList<DefinedProduct> = (clothes + electronics + accessories).toMutableList()

    override fun save(product: Product): Product {
        val productSaved = when(product) {
            is DraftProduct -> {
                val productId = ProductId(Identity(currentId.getAndIncrement().toString()))
                DefinedProduct(productId, product.sku, product.translations, product.category, product.price)
            }
            is DefinedProduct -> product
        }
        // A defined product replaces the stored product with the same id, at the same place
        val index = products.indexOfFirst { it.productId == productSaved.productId }
        if (index < 0) products.add(productSaved) else products[index] = productSaved
        return productSaved
    }

    override fun findAll(): List<Product> = products

    override fun findById(productId: ProductId): DefinedProduct? =
        products.firstOrNull { it.productId == productId }

    override fun findAllBy(category: Category): List<DefinedProduct> {
        return when (category) {
            Category.CLOTHING -> clothes
            Category.ELECTRONICS -> electronics
            Category.ACCESSORIES -> accessories
            Category.HOME,
            Category.BEAUTY,
            Category.TOYS,
            Category.SPORTS,
            Category.AUTOMOTIVE,
            Category.GROCERY,
            Category.HEALTH,
            Category.BOOKS,
            Category.NOT_DEFINED -> emptyList()
        }
    }

    override fun exists(product: Product): Boolean {
        return when(product) {
            is DraftProduct -> return products.any {
                it.name == product.name &&
                it.description == product.description &&
                it.category == product.category &&
                it.price == product.price
            }
            is DefinedProduct -> products.any { it == product }
        }
    }

    override fun notExists(product: Product): Boolean = !exists(product)

    override fun productExists(productId: ProductId): Boolean = products.any { it.productId == productId }

    override fun skuIsUsedByAnotherProduct(product: DefinedProduct): Boolean =
        products.any { it.sku == product.sku && it.productId != product.productId }

    override fun skuIsFree(sku: Sku): Boolean = products.none { it.sku == sku }
}

private val currentId = AtomicInteger(1)

// The SKU of the demo products, as given by the migration V25: the first letters of the category, then the id

val clothes = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("CLO-1"),
        name = Name("Organic Cotton T-shirt"),
        description = Description("Comfortable and eco-friendly"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(25)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("CLO-2"),
        name = Name("Waterproof Jacket"),
        description = Description("Resists rain and wind"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(79)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("CLO-3"),
        name = Name("Slim Jeans"),
        description = Description("Modern fit, stretch"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(49)), Currency.EUR),
    )
)

val electronics = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ELE-4"),
        name = Name("5G Smartphone"),
        description = Description("OLED display, 128 GB"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(399)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ELE-5"),
        name = Name("10\" Tablet"),
        description = Description("Ideal for multimedia"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(249)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ELE-6"),
        name = Name("Laptop"),
        description = Description("512 GB SSD, 16 GB RAM"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(799)), Currency.EUR),
    )
)

val accessories = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ACC-7"),
        name = Name("Smartwatch"),
        description = Description("Activity tracking and notifications"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(99)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ACC-8"),
        name = Name("Bluetooth Headphones"),
        description = Description("High-quality sound, 20h battery life"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(59)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        sku = Sku("ACC-9"),
        name = Name("Urban Backpack"),
        description = Description("Laptop compartment, modern design"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(39)), Currency.EUR),
    )
)