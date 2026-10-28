package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProductTranslation
import org.springframework.stereotype.Component

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
    fun toJson(product: Product): JsonProduct {
        return when(product) {
            is DefinedProduct -> product.toJson()
            else -> throw IllegalArgumentException("Unknown product type: ${product::class.java}")
        }
    }
}

fun <E> List<E>.toJsonProducts(): List<JsonProduct> {
    return this.map {
        when (it) {
            is DefinedProduct -> it.toJson()
            else -> throw IllegalArgumentException("Unknown product type: ${it!!::class.java}")
        }
    }
}

private fun DefinedProduct.toJson() = JsonProduct(
    productId.id.value,
    name.value,
    description.value,
    category.name,
    price.amount.value,
    price.currency.symbol,
    translations.asList().map { it.toJson() }
)

private fun ProductTranslation.toJson() =
    JsonProductTranslation(language.name.lowercase(), name.value, description.value)
