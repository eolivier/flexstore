package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
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
            is DefinedProduct -> JsonProduct(
                product.productId.id.value,
                product.name.value,
                product.description.value,
                product.category.name,
                product.price.amount.value,
                product.price.currency.symbol
            )
            else -> throw IllegalArgumentException("Unknown product type: ${product::class.java}")
        }
    }
}

fun <E> List<E>.toJsonProducts(): List<JsonProduct> {
    return this.map {
        when (it) {
            is DefinedProduct -> JsonProduct(
                it.productId.id.value,
                it.name.value,
                it.description.value,
                it.category.name,
                it.price.amount.value,
                it.price.currency.symbol
            )
            else -> throw IllegalArgumentException("Unknown product type: ${it!!::class.java}")
        }
    }
}