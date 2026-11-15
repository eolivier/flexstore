package org.flexstore.domain.cookie.usecase

import org.flexstore.domain.cookie.*
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer accepts or refuses the preference cookies: their choice is kept for 6 months
class RecordCookieConsentUseCase(
    private val cookieConsentRepository: CookieConsentRepository
) : UseCase<CookieConsentRecording> {

    override fun getPreConditions(): PreConditions<CookieConsentRecording> =
        PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<CookieConsentRecording> =
        NominalScenario(Step { recording ->
            cookieConsentRepository.save(recording.customer.customerId(), givenBy(recording))
        })

    override fun getPostConditions(): PostConditions<CookieConsentRecording> =
        PostConditions(consentIsStoredCondition())

    private fun givenBy(recording: CookieConsentRecording) =
        CookieConsent.Given.recordedAt(recording.choice, recording.consentMoment)

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<CookieConsentRecording> { recording ->
        recording.customer.verifyCanAct()
    }

    private fun consentIsStoredCondition() = PostCondition<CookieConsentRecording> { recording ->
        if (cookieConsentRepository.consentOf(recording.customer.customerId()) != givenBy(recording)) {
            throw CookieConsentRecordFailed(ErrorMessage("The cookie consent was not recorded."))
        }
    }
}
