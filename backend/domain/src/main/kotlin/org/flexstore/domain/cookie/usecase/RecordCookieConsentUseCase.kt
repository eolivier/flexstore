package org.flexstore.domain.cookie.usecase

import org.flexstore.domain.cookie.*
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.UserNotFoundException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer accepts or refuses the preference cookies: their choice is kept for 6 months
class RecordCookieConsentUseCase(
    private val cookieConsentRepository: CookieConsentRepository,
    private val userRepository: UserRepository
) : UseCase<CookieConsentRecording> {

    override fun getPreConditions(): PreConditions<CookieConsentRecording> =
        PreConditions(customerExistsCondition())

    override fun getNominalScenario(): NominalScenario<CookieConsentRecording> =
        NominalScenario(Step { recording ->
            cookieConsentRepository.save(recording.customer, givenBy(recording))
        })

    override fun getPostConditions(): PostConditions<CookieConsentRecording> =
        PostConditions(consentIsStoredCondition())

    private fun givenBy(recording: CookieConsentRecording) =
        CookieConsent.Given.recordedAt(recording.choice, recording.consentMoment)

    private fun customerExistsCondition() = PreCondition<CookieConsentRecording> { recording ->
        if (userRepository.notExists(recording.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${recording.customer.value} not found."))
        }
    }

    private fun consentIsStoredCondition() = PostCondition<CookieConsentRecording> { recording ->
        if (cookieConsentRepository.consentOf(recording.customer) != givenBy(recording)) {
            throw CookieConsentRecordFailed(ErrorMessage("The cookie consent was not recorded."))
        }
    }
}
