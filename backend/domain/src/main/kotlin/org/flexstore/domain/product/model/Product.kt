package org.flexstore.domain.product.model

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

private val NO_DESCRIPTION = Description("No description")

private fun inDefaultLanguageOnly(name: Name, description: Description) =
    ProductTranslations.inDefaultLanguage(ProductText(name, description))

// The regular price of a product, VAT included, the rate of VAT it includes, and its promotion (which lowers the
// price to pay only while it is running)
data class ProductPricing(val price: Price, val vatRate: VatRate, val promotion: Promotion) {
    // Without promotion
    constructor(price: Price, vatRate: VatRate) : this(price, vatRate, NoPromotion)

    // At the standard rate, the one of most products, without promotion
    constructor(price: Price) : this(price, VatRate.STANDARD)

    // What the customer pays for one product now: the regular price, less the promotion when it is running
    val currentPrice: Price get() = promotion.priceAfter(price)
}

sealed interface Product
// A product given by an administrator, before it is added to the catalog
data class DraftProduct(val sku: Sku,
                        val translations: ProductTranslations,
                        val category: Category,
                        val pricing: ProductPricing): Product {
    // A product at the standard rate of VAT
    constructor(sku: Sku, translations: ProductTranslations, category: Category, price: Price) :
        this(sku, translations, category, ProductPricing(price))

    // A product written in the default language only
    constructor(sku: Sku, name: Name, description: Description, category: Category, price: Price) :
        this(sku, inDefaultLanguageOnly(name, description), category, price)

    val price: Price get() = pricing.price

    // The name and the description in the default language
    val name: Name get() = translations.defaultTranslation().name
    val description: Description get() = translations.defaultTranslation().description
}

// A product of the catalog: its SKU, and its name and its description in each language it is translated into
data class DefinedProduct(
    val productId: ProductId,
    val sku: Sku,
    val translations: ProductTranslations,
    val category: Category,
    val pricing: ProductPricing): Product {
    // A product at the standard rate of VAT
    constructor(
        productId: ProductId,
        sku: Sku,
        translations: ProductTranslations,
        category: Category,
        price: Price
    ) : this(productId, sku, translations, category, ProductPricing(price))

    // A product written in the default language only
    constructor(
        productId: ProductId,
        sku: Sku,
        name: Name,
        description: Description,
        category: Category,
        price: Price
    ) : this(productId, sku, inDefaultLanguageOnly(name, description), category, price)

    // A product given with its id only: its SKU is its id
    constructor(productId: ProductId, name: Name, price: Price) :
        this(productId, Sku.from(productId.id.value), name, NO_DESCRIPTION, Category.NOT_DEFINED, price)

    val price: Price get() = pricing.price

    val vatRate: VatRate get() = pricing.vatRate

    val promotion: Promotion get() = pricing.promotion

    // The price the customer pays now: the price less the promotion when it is running
    val currentPrice: Price get() = pricing.currentPrice

    // The name and the description in the default language
    val name: Name get() = translations.defaultTranslation().name
    val description: Description get() = translations.defaultTranslation().description

    fun translationIn(language: Language) = translations.translationIn(language)

    // Renamed or described again in the default language
    fun renamed(name: Name) = inDefaultLanguage(ProductText(name, description))
    fun describedAs(description: Description) = inDefaultLanguage(ProductText(name, description))

    private fun inDefaultLanguage(text: ProductText) =
        copy(translations = translations.updatedWith(ProductTranslation(Language.DEFAULT, text)))
}
data class ProductId(val id: Identity) {
    companion object {
        fun nextId() = ProductId(Identity.nextId())
    }
}

enum class Category {
    ELECTRONICS, BOOKS, CLOTHING, HOME, BEAUTY, TOYS, SPORTS,
    AUTOMOTIVE, GROCERY, HEALTH, ACCESSORIES, NOT_DEFINED
}

// An administrator who asks for a SKU free in the catalog, for a product they create
data class SkuProposalRequest(val requester: UserId, val skuBasis: SkuBasis)

data class ProductAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidPromotionException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class ProductAlreadyExists(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class ProductCreationFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class InvalidSkuException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class SkuProposalFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class SkuAlreadyUsedException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class ProductNotFoundException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
