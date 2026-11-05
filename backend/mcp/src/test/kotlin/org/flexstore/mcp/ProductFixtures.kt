package org.flexstore.mcp

import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.Category
import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.product.model.PercentageOffPromotion
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.product.model.ProductPricing
import org.flexstore.domain.product.model.ProductTranslation
import org.flexstore.domain.product.model.ProductTranslations
import org.flexstore.domain.product.model.PromotionEnd
import org.flexstore.domain.product.model.PromotionPeriod
import org.flexstore.domain.product.model.PromotionStart
import org.flexstore.domain.product.model.Sku
import org.flexstore.domain.product.usecase.CreateProductUseCase
import org.flexstore.domain.product.usecase.ListProductsUseCase
import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.domain.product.usecase.ProposeSkuUseCase
import org.flexstore.domain.product.usecase.ReadProductUseCase
import org.flexstore.domain.product.usecase.UpdateProductUseCase
import org.flexstore.domain.sharedkernel.Amount
import org.flexstore.domain.sharedkernel.Currency
import org.flexstore.domain.sharedkernel.Description
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.sharedkernel.Price
import org.flexstore.domain.sharedkernel.VatRate
import org.flexstore.domain.user.repository.UserRepository

val deskLampId = ProductId(Identity("desk-lamp"))
val readingLampId = ProductId(Identity("reading-lamp"))

private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
private val englishDeskLamp = ProductTranslation(Language.EN, Name("Desk lamp"), Description("A lamp for the desk"))
private val frenchDeskLamp = ProductTranslation(Language.FR, Name("Lampe de bureau"), Description("Une lampe"))

// Translated into French
val deskLamp = DefinedProduct(
    deskLampId,
    Sku.from("HOM-1"),
    ProductTranslations(englishDeskLamp, frenchDeskLamp),
    Category.HOME,
    lampPrice
)

// Written in English only, and on promotion for a day around now
val readingLamp = DefinedProduct(
    readingLampId,
    Sku.from("HOM-2"),
    ProductTranslations(ProductTranslation(Language.EN, Name("Reading lamp"), Description("A lamp to read"))),
    Category.HOME,
    ProductPricing(lampPrice, VatRate.STANDARD, promotionRunningToday())
)

private fun promotionRunningToday(): PercentageOffPromotion {
    val started = PromotionStart(Instant.now().minus(1, ChronoUnit.DAYS))
    val ending = PromotionEnd(Instant.now().plus(1, ChronoUnit.DAYS))
    return PercentageOffPromotion(Percentage(BigDecimal(20)), PromotionPeriod(started, ending))
}

// The use cases of the domain over a catalog that holds these products only
fun productUseCasesOf(vararg products: DefinedProduct): ProductUseCases {
    val productRepository = mockk<ProductRepository>()
    every { productRepository.findAll() } returns products.toList()
    every { productRepository.findById(any()) } answers { products.firstOrNull { it.productId == firstArg() } }
    every { productRepository.productIsMissing(any()) } answers { products.none { it.productId == firstArg() } }
    val userRepository = mockk<UserRepository>()
    return ProductUseCases(
        CreateProductUseCase(productRepository, userRepository),
        ReadProductUseCase(productRepository),
        ListProductsUseCase(productRepository),
        UpdateProductUseCase(productRepository, userRepository),
        ProposeSkuUseCase(productRepository, userRepository)
    )
}
