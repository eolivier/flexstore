package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.Description
import org.flexstore.domain.sharedkernel.Name

// The languages of the shop. The catalog is written in the default language: a product is shown in it when it
// has no translation in the language of the customer
enum class Language {
    EN, FR;

    companion object {
        val DEFAULT = EN
    }
}

// The name and the description of a product in one language
data class ProductTranslation(val language: Language, val text: ProductText) {
    constructor(language: Language, name: Name, description: Description) :
        this(language, ProductText(name, description))

    val name get() = text.name
    val description get() = text.description

    fun isIn(language: Language) = this.language == language
}

// What a product says of itself: its name and its description
data class ProductText(val name: Name, val description: Description)

// The translations of a product: one per language at most, always one in the default language
data class ProductTranslations(private val translations: List<ProductTranslation>) {
    constructor(vararg translations: ProductTranslation) : this(translations.toList())

    init {
        require(translations.any { it.isIn(Language.DEFAULT) }) {
            "A product has a translation in the default language ${Language.DEFAULT}"
        }
        val languages = translations.map { it.language }
        require(languages.distinct() == languages) { "A product has one translation per language at most" }
    }

    fun defaultTranslation(): ProductTranslation = translations.first { it.isIn(Language.DEFAULT) }

    // The translation in the language asked, or the default one when the product is not translated in it
    fun translationIn(language: Language): ProductTranslation =
        translations.firstOrNull { it.isIn(language) } ?: defaultTranslation()

    // Each translation given replaces the one in its language, at its place; the translations in the other
    // languages are kept, and those in a new language come last
    fun updatedWith(vararg givenTranslations: ProductTranslation): ProductTranslations {
        val replacedTranslations = translations.map { translation ->
            givenTranslations.firstOrNull { it.language == translation.language } ?: translation
        }
        val languages = translations.map { it.language }
        val newTranslations = givenTranslations.filterNot { it.language in languages }
        return ProductTranslations(replacedTranslations + newTranslations)
    }

    fun updatedWith(givenTranslations: ProductTranslations) =
        updatedWith(*givenTranslations.translations.toTypedArray())

    // The translations in the other languages than the default one
    fun otherThanDefault(): List<ProductTranslation> = translations.filterNot { it.isIn(Language.DEFAULT) }

    fun asList(): List<ProductTranslation> = translations.toList()

    companion object {
        fun inDefaultLanguage(text: ProductText): ProductTranslations {
            val defaultTranslation = ProductTranslation(Language.DEFAULT, text)
            return ProductTranslations(defaultTranslation)
        }
    }
}
