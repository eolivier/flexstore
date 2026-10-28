package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage

sealed interface Product
// A product given by an administrator, before it is added to the catalog
data class DraftProduct(val translations: ProductTranslations,
                        val category: Category,
                        val price: Price): Product {
    // A product written in the default language only
    constructor(name: Name, description: Description, category: Category, price: Price) :
        this(ProductTranslations.inDefaultLanguage(ProductText(name, description)), category, price)

    // The name and the description in the default language
    val name: Name get() = translations.defaultTranslation().name
    val description: Description get() = translations.defaultTranslation().description
}

// A product of the catalog, with its name and its description in each language it is translated into
data class DefinedProduct(
    val productId: ProductId,
    val translations: ProductTranslations,
    val category: Category,
    val price: Price): Product {
    // A product written in the default language only
    constructor(productId: ProductId, name: Name, description: Description, category: Category, price: Price) :
        this(productId, ProductTranslations.inDefaultLanguage(ProductText(name, description)), category, price)
    constructor(productId: ProductId, name: Name, price: Price) :
        this(productId, name, Description("No description"), Category.NOT_DEFINED, price)

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

data class ProductAlreadyExists(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductCreationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ProductNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
