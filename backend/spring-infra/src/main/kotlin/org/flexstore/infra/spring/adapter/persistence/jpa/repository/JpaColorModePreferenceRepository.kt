package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference
import org.flexstore.domain.preference.ColorModePreferenceRepository
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ColorModePreferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaColorModePreferenceRepository : JpaRepository<ColorModePreferenceEntity, String> {
    fun findByUserId(userId: String): ColorModePreferenceEntity?
}

// One row per customer: a new choice replaces the row of the previous one
@Repository
@Transactional
class PostgresColorModePreferenceRepositoryAdapter(private val jpaRepo: JpaColorModePreferenceRepository) :
    ColorModePreferenceRepository {

    @Transactional(readOnly = true)
    override fun preferenceOf(customer: UserId): ColorModePreference {
        val entity = jpaRepo.findByUserId(customer.value) ?: return ColorModePreference.Unchosen
        return ColorModePreference.Chosen(ColorMode.valueOf(entity.colorMode))
    }

    override fun save(customer: UserId, colorMode: ColorMode) {
        val storedId = jpaRepo.findByUserId(customer.value)?.id ?: Identity.nextId().value
        jpaRepo.save(ColorModePreferenceEntity(storedId, customer.value, colorMode.name))
    }
}
