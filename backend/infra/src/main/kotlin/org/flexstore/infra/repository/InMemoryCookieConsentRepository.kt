package org.flexstore.infra.repository

import org.flexstore.domain.cookie.CookieConsent
import org.flexstore.domain.cookie.CookieConsentRepository
import org.flexstore.domain.user.model.UserId

class InMemoryCookieConsentRepository : CookieConsentRepository {

    private val consents = mutableMapOf<UserId, CookieConsent.Given>()

    override fun consentOf(customer: UserId): CookieConsent = consents[customer] ?: CookieConsent.Undecided

    override fun save(customer: UserId, cookieConsent: CookieConsent.Given) {
        consents[customer] = cookieConsent
    }
}
