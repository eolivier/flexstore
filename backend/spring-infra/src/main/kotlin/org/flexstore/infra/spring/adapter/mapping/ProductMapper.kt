package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class ProductMapper {
    fun toDomain(draftJsonProduct: DraftJsonProduct): DraftProduct {
        return DraftProduct(
            Name(draftJsonProduct.name),
            Description(draftJsonProduct.description),
            Category.valueOf(draftJsonProduct.category),
            Price(Amount(draftJsonProduct.price), Currency.valueOf(draftJsonProduct.currency)),
        )
    }
    fun toDomain(productId: ProductId, editedJsonProduct: EditedJsonProduct): DefinedProduct {
        return DefinedProduct(
            productId,
            Name(editedJsonProduct.name),
            Description(editedJsonProduct.description),
            Category.valueOf(editedJsonProduct.category),
            Price(Amount(editedJsonProduct.price), Currency.valueOf(editedJsonProduct.currency)),
        )
    }
    // In the default language: what the administrator created or edited
    fun toJson(product: Product): JsonProduct = listOf(product).toJsonProducts(Language.DEFAULT).single()
}

// The name and the description in the language asked, or in the default language when the product is not
// translated into it
fun <E> List<E>.toJsonProducts(language: Language): List<JsonProduct> {
    return this.map {
        when (it) {
            is DefinedProduct -> it.toJson(language)
            else -> throw IllegalArgumentException("Unknown product type: ${it!!::class.java}")
        }
    }
}

private fun DefinedProduct.toJson(language: Language): JsonProduct {
    val translation = translationIn(language)
    return JsonProduct(
        productId.id.value,
        translation.name.value,
        translation.description.value,
        category.name,
        price.amount.value,
        price.currency.symbol
    )
}

// The first language of the shop among those accepted by the customer (Accept-Language: "fr-FR,fr;q=0.9,en"),
// or the default language when there is none
fun languageAcceptedIn(acceptLanguage: String): Language {
    val acceptedRanges = runCatching { Locale.LanguageRange.parse(acceptLanguage) }.getOrDefault(emptyList())
    val acceptedCodes = acceptedRanges.map { it.range.substringBefore('-').uppercase() }
    val acceptedShopLanguages = acceptedCodes.flatMap { code -> Language.entries.filter { it.name == code } }
    return (acceptedShopLanguages + Language.DEFAULT).first()
}
