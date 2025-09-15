package org.flexstore.infra.repository

import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.*
import java.math.BigDecimal

class InMemoryProductRepository : ProductRepository {

    override fun findAll(): List<Product> {
        return clothes + electronics + accessories
    }

    override fun findAllBy(category: Category): List<Product> {
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
}

val clothes = listOf(
    Product(
        productId = ProductId(Identity("1")),
        name = Name("Organic Cotton T-shirt"),
        description = Description("Comfortable and eco-friendly"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(25)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("2")),
        name = Name("Waterproof Jacket"),
        description = Description("Resists rain and wind"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(79)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("3")),
        name = Name("Slim Jeans"),
        description = Description("Modern fit, stretch"),
        category = Category.CLOTHING,
        price = Price(Amount(BigDecimal(49)), Currency.EUR),
    )
)

val electronics = listOf(
    Product(
        productId = ProductId(Identity("4")),
        name = Name("5G Smartphone"),
        description = Description("OLED display, 128 GB"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(399)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("5")),
        name = Name("10\" Tablet"),
        description = Description("Ideal for multimedia"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(249)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("6")),
        name = Name("Laptop"),
        description = Description("512 GB SSD, 16 GB RAM"),
        category = Category.ELECTRONICS,
        price = Price(Amount(BigDecimal(799)), Currency.EUR),
    )
)

val accessories = listOf(
    Product(
        productId = ProductId(Identity("7")),
        name = Name("Smartwatch"),
        description = Description("Activity tracking and notifications"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(99)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("8")),
        name = Name("Bluetooth Headphones"),
        description = Description("High-quality sound, 20h battery life"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(59)), Currency.EUR),
    ),
    Product(
        productId = ProductId(Identity("9")),
        name = Name("Urban Backpack"),
        description = Description("Laptop compartment, modern design"),
        category = Category.ACCESSORIES,
        price = Price(Amount(BigDecimal(39)), Currency.EUR),
    )
)