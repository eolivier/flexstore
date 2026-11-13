package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

// A sign-in waiting for its second factor: the id is the reference given to the browser, the row is deleted
// once the code is verified
@Entity
@Table(name = "two_factor_challenge")
data class TwoFactorChallengeEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(name = "issued_at", nullable = false, updatable = false)
    val issuedAt: Instant
) : TimestampedEntity()
