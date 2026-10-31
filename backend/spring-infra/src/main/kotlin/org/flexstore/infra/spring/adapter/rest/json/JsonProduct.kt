package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

// name and description: in the language asked by the customer (Accept-Language), or in the default language
// (en) when the product is not translated into it
data class JsonProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("sku") val sku: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String
)

// A product as the administrator edits it: its translations in every language, the default one (en) included
data class JsonAdminProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("sku") val sku: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>,
    @JsonProperty("vatRate") val vatRate: String
)

// The name and the description of a product in one language: en or fr
data class JsonProductTranslation @JsonCreator constructor(
    @JsonProperty("language") val language: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String
)

// sku: as typed, the spaces around dropped and the letters put in upper case. vatRate: STANDARD (when not
// given), INTERMEDIATE, REDUCED or SUPER_REDUCED. translations: the name and the description in each language of
// the product, the default one (en) required
data class DraftJsonProduct @JsonCreator constructor(
    @JsonProperty("sku") val sku: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>,
    @JsonProperty("vatRate") val vatRate: String? = null
)

// The fields an administrator edits; the id is given by the path. sku: as typed, like a created product.
// vatRate: like a created product. translations: every translation of the product, the default one (en)
// required; a translation not given is removed
data class EditedJsonProduct @JsonCreator constructor(
    @JsonProperty("sku") val sku: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>,
    @JsonProperty("vatRate") val vatRate: String? = null
)

// A SKU proposed to an administrator, free in the catalog when it is proposed
data class JsonSkuProposal @JsonCreator constructor(@JsonProperty("sku") val sku: String)
