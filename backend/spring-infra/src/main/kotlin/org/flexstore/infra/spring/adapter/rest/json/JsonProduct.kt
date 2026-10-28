package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

// name and description: in the default language (en); translations: in every language of the product, the
// default one included, so that the front shows the product in the language of the customer
data class JsonProduct @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("translations") val translations: List<JsonProductTranslation>
)

// language: en or fr
data class JsonProductTranslation @JsonCreator constructor(
    @JsonProperty("language") val language: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String
)

data class DraftJsonProduct @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String
)

// The fields an administrator edits; the id is given by the path
data class EditedJsonProduct @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("category") val category: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String
)
