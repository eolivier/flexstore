package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonLoginResponse(
    // JWT to send in the Authorization header: "Bearer <token>"
    @JsonProperty("token") val token: String,
    @JsonProperty("expiresAt") val expiresAt: String,
    @JsonProperty("user") val user: JsonUser
)
