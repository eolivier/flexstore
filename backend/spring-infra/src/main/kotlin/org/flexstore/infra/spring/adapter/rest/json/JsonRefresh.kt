package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.user.model.RefreshToken

// The refresh token that a signed-in user presents to renew their access, or to sign out
data class JsonRefreshRequest @JsonCreator constructor(@JsonProperty("refreshToken") val refreshToken: String) {
    fun toRefreshToken() = RefreshToken(refreshToken)
}

// A new token and the new refresh token that replaces the one presented
data class JsonRefreshResponse(
    @JsonProperty("token") val token: String,
    @JsonProperty("expiresAt") val expiresAt: String,
    @JsonProperty("refreshToken") val refreshToken: String,
    @JsonProperty("refreshExpiresAt") val refreshExpiresAt: String
)
