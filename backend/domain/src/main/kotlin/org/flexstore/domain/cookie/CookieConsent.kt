package org.flexstore.domain.cookie

import java.time.Instant
import java.time.ZoneOffset
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException

// The choice of a customer about the preference cookies (the essential ones need no consent)
enum class CookieChoice { ACCEPTED, REFUSED }

// A moment of the cookie consent: when the customer chose, when their consent is read
data class ConsentMoment(val value: Instant) {
    companion object {
        fun now() = ConsentMoment(InstantProvider.now())
    }
}

// The moment from which the customer is asked again: their consent lasts 6 months
data class ConsentExpiry(val value: Instant) {

    fun isReachedAt(consentMoment: ConsentMoment) = consentMoment.value >= value

    companion object {
        private const val VALIDITY_MONTHS = 6L

        fun sixMonthsAfter(consentMoment: ConsentMoment): ConsentExpiry {
            val expiry = consentMoment.value.atZone(ZoneOffset.UTC).plusMonths(VALIDITY_MONTHS)
            return ConsentExpiry(expiry.toInstant())
        }
    }
}

// What a signed-in customer decided about the cookies: a choice that expires, or nothing (yet, or any more)
sealed interface CookieConsent {

    fun validAt(consentMoment: ConsentMoment): CookieConsent

    data class Given(val choice: CookieChoice, val expiry: ConsentExpiry) : CookieConsent {
        override fun validAt(consentMoment: ConsentMoment) =
            if (expiry.isReachedAt(consentMoment)) Undecided else this

        companion object {
            fun recordedAt(choice: CookieChoice, consentMoment: ConsentMoment) =
                Given(choice, ConsentExpiry.sixMonthsAfter(consentMoment))
        }
    }

    // The customer was never asked, or their choice expired: they are asked
    data object Undecided : CookieConsent {
        override fun validAt(consentMoment: ConsentMoment): CookieConsent = this
    }
}

// A customer chooses about the cookies
data class CookieConsentRecording(
    val customer: UserId,
    val choice: CookieChoice,
    val consentMoment: ConsentMoment
)

// A customer's consent is read, as valid at a moment
data class CookieConsentReading(val customer: UserId, val consentMoment: ConsentMoment)

data class CookieConsentRecordFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
