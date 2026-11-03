package org.flexstore.domain.cookie

import org.flexstore.domain.user.model.UserId

interface CookieConsentRepository {

    // The consent stored for a customer, expired or not: undecided when they never chose
    fun consentOf(customer: UserId): CookieConsent

    // Replaces the consent of the customer
    fun save(customer: UserId, cookieConsent: CookieConsent.Given)
}
