package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// The review of a product by a customer who bought it: a rating from 1 to 5 and a comment
@Entity
@Table(name = "product_review")
data class ProductReviewEntity(
    @Id
    val id: String,

    @Column(name = "product_id", nullable = false, updatable = false)
    val productId: String,

    // The reviewer: their name is read with the review
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: UserEntity,

    @Column(nullable = false)
    val rating: Int,

    @Column(nullable = false)
    val comment: String,

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    val reviewedAt: Instant
) : TimestampedEntity()
