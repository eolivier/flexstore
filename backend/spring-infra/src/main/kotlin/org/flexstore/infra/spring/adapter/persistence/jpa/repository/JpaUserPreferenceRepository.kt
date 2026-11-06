package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference
import org.flexstore.domain.preference.LanguagePreference
import org.flexstore.domain.preference.UserPreferenceRepository
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserPreferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaUserPreferenceRepository : JpaRepository<UserPreferenceEntity, String> {
    fun findByUserId(userId: String): UserPreferenceEntity?
}

// One user_preference row per customer: a new choice replaces the previous one, the other preference is kept
@Repository
@Transactional
class PostgresUserPreferenceRepositoryAdapter(private val jpaRepo: JpaUserPreferenceRepository) :
    UserPreferenceRepository {

    @Transactional(readOnly = true)
    override fun preferenceOf(customer: UserId): ColorModePreference {
        val colorMode = jpaRepo.findByUserId(customer.value)?.colorMode ?: return ColorModePreference.Unchosen
        return ColorModePreference.Chosen(ColorMode.valueOf(colorMode))
    }

    override fun save(customer: UserId, colorMode: ColorMode) {
        val stored = storedPreferenceOf(customer)
        jpaRepo.save(stored.copy(colorMode = colorMode.name))
    }

    @Transactional(readOnly = true)
    override fun languagePreferenceOf(customer: UserId): LanguagePreference {
        val language = jpaRepo.findByUserId(customer.value)?.language ?: return LanguagePreference.Unchosen
        return LanguagePreference.Chosen(Language.valueOf(language))
    }

    override fun saveLanguage(customer: UserId, language: Language) {
        val stored = storedPreferenceOf(customer)
        jpaRepo.save(stored.copy(language = language.name))
    }

    private fun storedPreferenceOf(customer: UserId) =
        jpaRepo.findByUserId(customer.value) ?: UserPreferenceEntity(Identity.nextId().value, customer.value)
}
