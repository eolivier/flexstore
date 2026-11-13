package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.product.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductTranslationEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.domain.product.*
import org.flexstore.infra.spring.adapter.mapping.promotionOf
import org.flexstore.domain.sharedkernel.*

fun ProductEntity.toDomain(): DefinedProduct {
    // In the default language first, so that a product reads its translations in the same order as it gave them
    val storedTranslations = translations.map { it.toDomain() }.sortedByDescending { it.isIn(Language.DEFAULT) }
    val productTranslations = ProductTranslations(storedTranslations)
    val regularPrice = Price(Amount(price), Currency.valueOf(currency))
    val promotion = promotionOf(promotionPercentage, promotionStartsAt, promotionEndsAt)
    val pricing = ProductPricing(regularPrice, VatRate.valueOf(vatRate), promotion)
    val productId = ProductId(Identity(id))
    return DefinedProduct(productId, Sku(sku), productTranslations, Category.valueOf(category), pricing)
}

private fun ProductTranslationEntity.toDomain() =
    ProductTranslation(Language.valueOf(language.uppercase()), ProductText(Name(name), Description(description)))
