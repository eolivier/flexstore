package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.cookie.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CookieConsentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaCookieConsentRepository : JpaRepository<CookieConsentEntity, String> {
    fun findByUserId(userId: String): CookieConsentEntity?
}

// One row per customer: a new choice replaces the row of the previous one
@Repository
@Transactional
class PostgresCookieConsentRepositoryAdapter(private val jpaRepo: JpaCookieConsentRepository) :
    CookieConsentRepository {

    @Transactional(readOnly = true)
    override fun consentOf(customer: UserId): CookieConsent {
        val entity = jpaRepo.findByUserId(customer.value) ?: return CookieConsent.Undecided
        return CookieConsent.Given(CookieChoice.valueOf(entity.choice), ConsentExpiry(entity.expiresAt))
    }

    override fun save(customer: UserId, cookieConsent: CookieConsent.Given) {
        val storedId = jpaRepo.findByUserId(customer.value)?.id ?: Identity.nextId().value
        val entity = CookieConsentEntity(
            id = storedId,
            userId = customer.value,
            choice = cookieConsent.choice.name,
            expiresAt = cookieConsent.expiry.value
        )
        jpaRepo.save(entity)
    }
}
