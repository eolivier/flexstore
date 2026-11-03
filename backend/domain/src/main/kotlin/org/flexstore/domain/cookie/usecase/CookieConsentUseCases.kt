package org.flexstore.domain.cookie.usecase

import org.flexstore.domain.cookie.*
import org.flexstore.domain.user.model.UserId

// The use cases of the cookie consent of a signed-in customer: their consent is read back once recorded
class CookieConsentUseCases(
    private val readCookieConsentUseCase: ReadCookieConsentUseCase,
    private val recordCookieConsentUseCase: RecordCookieConsentUseCase
) {
    fun consentOf(customer: UserId): CookieConsent {
        val reading = CookieConsentReading(customer, ConsentMoment.now())
        return readCookieConsentUseCase.unfold(reading)
    }

    fun record(customer: UserId, choice: CookieChoice): CookieConsent {
        val consentMoment = ConsentMoment.now()
        recordCookieConsentUseCase.unfold(CookieConsentRecording(customer, choice, consentMoment))
        return readCookieConsentUseCase.unfold(CookieConsentReading(customer, consentMoment))
    }
}
