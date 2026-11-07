package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.preference.*
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserSettingEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserSettingName
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The preferences are the color_mode and language settings of the customer: a new choice replaces the value of
// its setting, the other one is kept
@Repository
@Transactional
class PostgresUserPreferenceRepositoryAdapter(private val jpaRepo: JpaUserSettingRepository) :
    UserPreferenceRepository {

    @Transactional(readOnly = true)
    override fun preferencesOf(customer: UserId): UserPreferences {
        val colorMode = settingOf(customer, UserSettingName.COLOR_MODE)
            ?.let { ColorModePreference.Chosen(ColorMode.valueOf(it.value)) } ?: ColorModePreference.Unchosen
        val language = settingOf(customer, UserSettingName.LANGUAGE)
            ?.let { LanguagePreference.Chosen(Language.valueOf(it.value)) } ?: LanguagePreference.Unchosen
        return UserPreferences(colorMode, language)
    }

    override fun save(customer: UserId, preference: UserPreference) {
        when (preference) {
            is UserPreference.DisplayMode ->
                replace(customer, UserSettingName.COLOR_MODE, preference.colorMode.name)
            is UserPreference.PreferredLanguage ->
                replace(customer, UserSettingName.LANGUAGE, preference.language.name)
        }
    }

    private fun settingOf(customer: UserId, settingName: UserSettingName) =
        jpaRepo.findByUserIdAndName(customer.value, settingName.stored)

    private fun replace(customer: UserId, settingName: UserSettingName, value: String) {
        val storedId = settingOf(customer, settingName)?.id ?: Identity.nextId().value
        jpaRepo.save(UserSettingEntity(storedId, customer.value, settingName.stored, value))
    }
}
