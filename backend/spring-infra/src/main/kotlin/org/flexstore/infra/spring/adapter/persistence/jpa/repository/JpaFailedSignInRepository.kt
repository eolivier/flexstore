package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.user.repository.FailedSignInRepository
import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The failed sign-ins in a row are a column of the user table, written by this statement only (the user entity
// does not update it). The update date is set here, as Hibernate does not set it on a bulk update
@Repository
interface JpaFailedSignInRepository : JpaRepository<UserEntity, String> {

    @Query("SELECT u.failedSignInCount FROM UserEntity u WHERE u.id = :userId")
    fun findCountOf(userId: String): Int?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE \"user\" SET failed_sign_in_count = :count, updated_at = now() WHERE id = :userId",
        nativeQuery = true
    )
    fun giveCount(userId: String, count: Int): Int
}

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
