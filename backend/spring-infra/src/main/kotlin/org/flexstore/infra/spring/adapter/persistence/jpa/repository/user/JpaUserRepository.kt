package org.flexstore.infra.spring.adapter.persistence.jpa.repository.user

import org.flexstore.domain.user.repository.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
}
