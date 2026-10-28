package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage

private val NO_DESCRIPTION = Description("No description")

private fun inDefaultLanguageOnly(name: Name, description: Description) =
    ProductTranslations.inDefaultLanguage(ProductText(name, description))

sealed interface Product
// A product given by an administrator, before it is added to the catalog
data class DraftProduct(val sku: Sku,
                        val translations: ProductTranslations,
                        val category: Category,
                        val price: Price): Product {
    // A product written in the default language only
    constructor(sku: Sku, name: Name, description: Description, category: Category, price: Price) :
        this(sku, inDefaultLanguageOnly(name, description), category, price)

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
    val price: Price): Product {
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

// Stock Keeping Unit: the reference the shop gives to a product to identify it in its stock and its orders,
// unique in the catalog (TSHIRT-ORGANIC-M). Upper-case letters and digits, in groups separated by single hyphens
data class Sku(val value: String) {

    fun isWellFormed() = value.length <= MAX_LENGTH && WELL_FORMED.matches(value)

    fun isMalformed() = !isWellFormed()

    companion object {
        const val MAX_LENGTH = 32

        private val WELL_FORMED = Regex("[A-Z0-9]+(-[A-Z0-9]+)*")

        // As typed by an administrator: the spaces around are dropped, the letters put in upper case
        fun from(typedSku: String) = Sku(typedSku.trim().uppercase())
    }
}

enum class Category {
    ELECTRONICS, BOOKS, CLOTHING, HOME, BEAUTY, TOYS, SPORTS,
    AUTOMOTIVE, GROCERY, HEALTH, ACCESSORIES, NOT_DEFINED
}

data class ProductAlreadyExists(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductCreationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvalidSkuException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class SkuAlreadyUsedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
