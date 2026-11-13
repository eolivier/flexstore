package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.twofactor.TwoFactorEnrollment
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.usecase.TwoFactorCodeVerification
import org.flexstore.domain.user.model.PlainPassword
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// What a customer or administrator reads once an enrollment started: the secret to type manually, and the
// link a QR code is generated from, on the front end
data class JsonTwoFactorEnrollment(
    @JsonProperty("secret") val secret: String,
    @JsonProperty("provisioningUri") val provisioningUri: String
) {
    companion object {
        fun of(enrollment: TwoFactorEnrollment) =
            JsonTwoFactorEnrollment(enrollment.secret.value, enrollment.provisioningUri.value)
    }
}

data class JsonTwoFactorCode @JsonCreator constructor(@JsonProperty("code") val code: String) {
    fun toTwoFactorCode() = TwoFactorCode(code)
}

data class JsonTwoFactorDisableRequest @JsonCreator constructor(
    @JsonProperty("currentPassword") val currentPassword: String
) {
    fun toPlainPassword() = PlainPassword(currentPassword)
}

data class JsonTwoFactorChallengeVerification @JsonCreator constructor(
    @JsonProperty("challengeId") val challengeId: String,
    @JsonProperty("code") val code: String
) {
    fun toTwoFactorCodeVerification() =
        TwoFactorCodeVerification(TwoFactorChallengeId(Identity(challengeId)), TwoFactorCode(code))
}
