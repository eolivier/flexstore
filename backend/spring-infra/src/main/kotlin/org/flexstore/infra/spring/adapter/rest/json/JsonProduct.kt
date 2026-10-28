package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

// name and description: in the language asked by the customer (Accept-Language), or in the default language
// (en) when the product is not translated into it
data class JsonProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String
)

// A product as the administrator edits it: its translations in every language, the default one (en) included
data class JsonAdminProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>
)

// The name and the description of a product in one language: en or fr
data class JsonProductTranslation @JsonCreator constructor(
    @JsonProperty("language") val language: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String
)

// translations: the name and the description in each language of the product, the default one (en) required
data class DraftJsonProduct @JsonCreator constructor(
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>
)

// The fields an administrator edits; the id is given by the path. translations: every translation of the
// product, the default one (en) required; a translation not given is removed
data class EditedJsonProduct @JsonCreator constructor(
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>
)
