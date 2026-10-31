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

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(nullable = false)
    val rating: Int,

    @Column(nullable = false)
    val comment: String,

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    val reviewedAt: Instant,

    // The reviewer, read to name the review; the column is written through userId
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: UserEntity? = null
) : TimestampedEntity()
