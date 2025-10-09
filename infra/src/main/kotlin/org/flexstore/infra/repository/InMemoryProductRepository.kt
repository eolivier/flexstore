package org.flexstore.infra.repository

import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.*
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

class InMemoryProductRepository : ProductRepository {

    private val products: MutableList<DefinedProduct> = (clothes + electronics + accessories).toMutableList()

    override fun save(product: Product): Product {
        val productSaved = when(product) {
            is DraftProduct -> {
                DefinedProduct(
                    productId = ProductId(Identity(currentId.getAndIncrement().toString())),
                    name = product.name,
                    description = product.description,
                    category = product.category,
                    price = product.price,
                )
            }
            is DefinedProduct -> product
        }
        products.add(productSaved)
        return productSaved
    }

    override fun findAll(): List<Product> = products

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
}

private val currentId = AtomicInteger(1)


val clothes = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Organic Cotton T-shirt"),
        description = Description("Comfortable and eco-friendly"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(25)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Waterproof Jacket"),
        description = Description("Resists rain and wind"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(79)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Slim Jeans"),
        description = Description("Modern fit, stretch"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(49)), Currency.EUR),
    )
)

val electronics = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("5G Smartphone"),
        description = Description("OLED display, 128 GB"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(399)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("10\" Tablet"),
        description = Description("Ideal for multimedia"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(249)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Laptop"),
        description = Description("512 GB SSD, 16 GB RAM"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(799)), Currency.EUR),
    )
)

val accessories = listOf(
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Smartwatch"),
        description = Description("Activity tracking and notifications"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(99)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Bluetooth Headphones"),
        description = Description("High-quality sound, 20h battery life"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(59)), Currency.EUR),
    ),
    DefinedProduct(
        productId = ProductId(Identity(currentId.getAndIncrement().toString())),
        name = Name("Urban Backpack"),
        description = Description("Laptop compartment, modern design"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(39)), Currency.EUR),
    )
)