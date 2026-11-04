package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

// SIGNED_IN: token, expiresAt and user are filled, challengeId is absent
// TWO_FACTOR_REQUIRED: only challengeId is filled, the password was right but the code of the authenticator app
// is still needed; POST it with the code to /api/users/login/two-factor-authentication to complete the sign-in
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonLoginResponse(
    @JsonProperty("status") val status: String,
    // JWT to send in the Authorization header: "Bearer <token>"
    @JsonProperty("token") val token: String? = null,
    @JsonProperty("expiresAt") val expiresAt: String? = null,
    // Opaque secret that renews the token once it expired (POST /api/users/refresh); it works once
    @JsonProperty("refreshToken") val refreshToken: String? = null,
    @JsonProperty("refreshExpiresAt") val refreshExpiresAt: String? = null,
    @JsonProperty("user") val user: JsonUser? = null,
    @JsonProperty("challengeId") val challengeId: String? = null
) {
    companion object {
        fun signedIn(
            token: String,
            expiresAt: String,
            refreshToken: String,
            refreshExpiresAt: String,
            user: JsonUser
        ) = JsonLoginResponse(
            status = "SIGNED_IN", token = token, expiresAt = expiresAt,
            refreshToken = refreshToken, refreshExpiresAt = refreshExpiresAt, user = user
        )

        fun twoFactorChallengeRequired(challengeId: String) =
            JsonLoginResponse(status = "TWO_FACTOR_REQUIRED", challengeId = challengeId)
    }
}
