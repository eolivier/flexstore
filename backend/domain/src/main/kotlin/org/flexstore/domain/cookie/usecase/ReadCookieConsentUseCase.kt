package org.flexstore.domain.cookie.usecase

import org.flexstore.domain.cookie.*
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.UserNotFoundException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer reads their cookie consent: undecided once their choice is 6 months old
class ReadCookieConsentUseCase(
    private val cookieConsentRepository: CookieConsentRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CookieConsentReading, CookieConsent> {

    override fun getPreConditions(): PreConditions<CookieConsentReading> =
        PreConditions(customerExistsCondition())

    override fun getNominalQuery() = NominalQuery<CookieConsentReading, CookieConsent> { reading ->
        cookieConsentRepository.consentOf(reading.customer).validAt(reading.consentMoment)
    }

    override fun getPostConditions(): PostConditions<CookieConsent> = PostConditions.none()

    private fun customerExistsCondition() = PreCondition<CookieConsentReading> { reading ->
        if (userRepository.notExists(reading.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${reading.customer.value} not found."))
        }
    }
}
