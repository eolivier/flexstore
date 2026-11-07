package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.preference.*
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
    override fun preferencesOf(customer: UserId): UserPreferences {
        val entity = jpaRepo.findByUserId(customer.value) ?: return UserPreferences(
            ColorModePreference.Unchosen, LanguagePreference.Unchosen
        )
        return UserPreferences(colorModePreferenceOf(entity), languagePreferenceOf(entity))
    }

    override fun save(customer: UserId, preference: UserPreference) {
        val stored = jpaRepo.findByUserId(customer.value)
            ?: UserPreferenceEntity(Identity.nextId().value, customer.value)
        jpaRepo.save(
            when (preference) {
                is UserPreference.DisplayMode -> stored.copy(colorMode = preference.colorMode.name)
                is UserPreference.PreferredLanguage -> stored.copy(language = preference.language.name)
            }
        )
    }

    private fun colorModePreferenceOf(entity: UserPreferenceEntity) =
        entity.colorMode?.let { ColorModePreference.Chosen(ColorMode.valueOf(it)) }
            ?: ColorModePreference.Unchosen

    private fun languagePreferenceOf(entity: UserPreferenceEntity) =
        entity.language?.let { LanguagePreference.Chosen(Language.valueOf(it)) } ?: LanguagePreference.Unchosen
}
