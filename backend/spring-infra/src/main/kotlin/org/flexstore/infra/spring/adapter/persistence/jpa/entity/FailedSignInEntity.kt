package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The failed sign-ins in a row of an account: the row only exists while the count is above 0
@Entity
@Table(name = "failed_sign_in")
data class FailedSignInEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    @Column(name = "attempt_count", nullable = false)
    val attemptCount: Int
) : TimestampedEntity()
