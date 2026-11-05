package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.repository.FailedSignInRepository
import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.FailedSignInEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaFailedSignInRepository : JpaRepository<FailedSignInEntity, String> {
    fun findByUserId(userId: String): FailedSignInEntity?

    @Modifying
    fun deleteAllByUserId(userId: String)
}

// One row per account, replaced by each new count
@Repository
@Transactional
class PostgresFailedSignInRepositoryAdapter(private val jpaRepo: JpaFailedSignInRepository) :
    FailedSignInRepository {

    @Transactional(readOnly = true)
    override fun countOf(userId: UserId): FailedSignInCount {
        val stored = jpaRepo.findByUserId(userId.value) ?: return FailedSignInCount.none()
        return FailedSignInCount(stored.attemptCount)
    }

    override fun save(userId: UserId, failedSignInCount: FailedSignInCount) {
        val storedId = jpaRepo.findByUserId(userId.value)?.id ?: Identity.nextId().value
        jpaRepo.save(FailedSignInEntity(storedId, userId.value, failedSignInCount.value))
    }

    override fun reset(userId: UserId) {
        jpaRepo.deleteAllByUserId(userId.value)
    }
}
