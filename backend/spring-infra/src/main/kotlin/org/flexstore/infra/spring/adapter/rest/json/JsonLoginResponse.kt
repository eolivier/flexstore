package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonLoginResponse(
    // JWT to send in the Authorization header: "Bearer <token>"
    @JsonProperty("token") val token: String,
    @JsonProperty("expiresAt") val expiresAt: String,
    // Opaque secret that renews the token once it expired (POST /api/users/refresh); it works once
    @JsonProperty("refreshToken") val refreshToken: String,
    @JsonProperty("refreshExpiresAt") val refreshExpiresAt: String,
    @JsonProperty("user") val user: JsonUser
)
