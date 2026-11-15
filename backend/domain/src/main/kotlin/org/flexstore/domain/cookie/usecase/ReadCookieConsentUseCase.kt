package org.flexstore.domain.cookie.usecase

import org.flexstore.domain.cookie.*
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.entity.*

// A signed-in customer reads their cookie consent: undecided once their choice is 6 months old
class ReadCookieConsentUseCase(
    private val cookieConsentRepository: CookieConsentRepository
) : QueryUseCase<CookieConsentReading, CookieConsent> {

    override fun getPreConditions(): PreConditions<CookieConsentReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CookieConsentReading, CookieConsent> { reading ->
        cookieConsentRepository.consentOf(reading.customer.customerId()).validAt(reading.consentMoment)
    }

    override fun getPostConditions(): PostConditions<CookieConsent> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<CookieConsentReading> { reading ->
        reading.customer.verifyCanAct()
    }
}
