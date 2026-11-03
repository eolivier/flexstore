package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.cookie.CookieChoice
import org.flexstore.domain.cookie.CookieConsent

// What the customer decided about the cookies: ACCEPTED or REFUSED until the expiry, UNDECIDED (without expiry)
// when they never chose or their choice expired
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonCookieConsent(
    @JsonProperty("choice") val choice: String,
    @JsonProperty("expiresAt") val expiresAt: String?
) {
    companion object {
        private const val UNDECIDED = "UNDECIDED"

        fun of(cookieConsent: CookieConsent) = when (cookieConsent) {
            is CookieConsent.Given -> given(cookieConsent)
            CookieConsent.Undecided -> JsonCookieConsent(UNDECIDED, null)
        }

        private fun given(cookieConsent: CookieConsent.Given) =
            JsonCookieConsent(cookieConsent.choice.name, cookieConsent.expiry.value.toString())
    }
}

// The choice of the customer about the preference cookies
data class JsonCookieChoice @JsonCreator constructor(@JsonProperty("choice") val choice: CookieChoice)
