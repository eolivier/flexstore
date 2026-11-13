package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

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
