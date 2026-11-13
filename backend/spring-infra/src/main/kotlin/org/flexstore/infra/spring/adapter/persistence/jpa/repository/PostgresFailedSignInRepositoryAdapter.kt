package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.user.repository.FailedSignInRepository
import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional
class PostgresFailedSignInRepositoryAdapter(private val jpaRepo: JpaFailedSignInRepository) :
    FailedSignInRepository {

    @Transactional(readOnly = true)
    override fun countOf(userId: UserId): FailedSignInCount =
        jpaRepo.findCountOf(userId.value)?.let { FailedSignInCount(it) } ?: FailedSignInCount.none()

    override fun save(userId: UserId, failedSignInCount: FailedSignInCount) {
        jpaRepo.giveCount(userId.value, failedSignInCount.value)
    }

    override fun reset(userId: UserId) {
        jpaRepo.giveCount(userId.value, FailedSignInCount.none().value)
    }
}
