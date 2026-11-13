package org.flexstore.infra.spring.adapter.persistence.jpa.repository.usersetting

import org.flexstore.domain.cookie.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserSettingEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserSettingName
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The consent is the cookie_choice setting of the customer: a new choice replaces its value and its expiry
@Repository
@Transactional
class PostgresCookieConsentRepositoryAdapter(private val jpaRepo: JpaUserSettingRepository) :
    CookieConsentRepository {

    private val settingName = UserSettingName.COOKIE_CHOICE.stored

    @Transactional(readOnly = true)
    override fun consentOf(customer: UserId): CookieConsent {
        val setting = jpaRepo.findByUserIdAndName(customer.value, settingName) ?: return CookieConsent.Undecided
        val expiresAt = setting.expiresAt ?: return CookieConsent.Undecided
        return CookieConsent.Given(CookieChoice.valueOf(setting.value), ConsentExpiry(expiresAt))
    }

    override fun save(customer: UserId, cookieConsent: CookieConsent.Given) {
        val storedId = jpaRepo.findByUserIdAndName(customer.value, settingName)?.id ?: Identity.nextId().value
        val setting = UserSettingEntity(
            id = storedId,
            userId = customer.value,
            name = settingName,
            value = cookieConsent.choice.name,
            expiresAt = cookieConsent.expiry.value
        )
        jpaRepo.save(setting)
    }
}
