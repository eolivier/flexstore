package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference
import org.flexstore.domain.preference.ColorModePreferenceRepository
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

// One user_preference row per customer: a new choice replaces the display mode of the previous one
@Repository
@Transactional
class PostgresColorModePreferenceRepositoryAdapter(private val jpaRepo: JpaUserPreferenceRepository) :
    ColorModePreferenceRepository {

    @Transactional(readOnly = true)
    override fun preferenceOf(customer: UserId): ColorModePreference {
        val entity = jpaRepo.findByUserId(customer.value) ?: return ColorModePreference.Unchosen
        return ColorModePreference.Chosen(ColorMode.valueOf(entity.colorMode))
    }

    override fun save(customer: UserId, colorMode: ColorMode) {
        val storedId = jpaRepo.findByUserId(customer.value)?.id ?: Identity.nextId().value
        jpaRepo.save(UserPreferenceEntity(storedId, customer.value, colorMode.name))
    }
}
