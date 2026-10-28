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
