package org.flexstore.infra.spring.adapter.persistence.jpa.repository.usersetting

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserSettingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserSettingRepository : JpaRepository<UserSettingEntity, String> {
    fun findByUserIdAndName(userId: String, name: String): UserSettingEntity?
}
