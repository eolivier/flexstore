package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ProductReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaProductReviewRepository : JpaRepository<ProductReviewEntity, String> {
    fun findByProductId(productId: String): List<ProductReviewEntity>

    fun existsByUserIdAndProductId(userId: String, productId: String): Boolean
}

// The reviews, kept in PostgreSQL; the reviewer is named from the account of the customer when it is read
@Repository
@Transactional
class PostgresReviewRepositoryAdapter(
    private val jpaRepo: JpaProductReviewRepository
) : ReviewRepository {

    override fun save(review: Review) {
        jpaRepo.save(review.toEntity())
    }

    @Transactional(readOnly = true)
    override fun findAllAbout(productId: ProductId) =
        Reviews(jpaRepo.findByProductId(productId.id.value).map { it.toDomain() }).newestFirst()

    @Transactional(readOnly = true)
    override fun findAll() = Reviews(jpaRepo.findAll().map { it.toDomain() })

    @Transactional(readOnly = true)
    override fun reviewExists(reviewId: ReviewId) = jpaRepo.existsById(reviewId.value)

    @Transactional(readOnly = true)
    override fun hasReviewed(customer: UserId.ValidUserId, productId: ProductId) =
        jpaRepo.existsByUserIdAndProductId(customer.value, productId.id.value)

    private fun Review.toEntity() = ProductReviewEntity(
        reviewId.value, productId.id.value, reviewer.userId.value, rating.value, comment.value, reviewedAt.value
    )

    private fun ProductReviewEntity.toDomain(): Review {
        val fullName = user?.let { FullName(FirstName(it.firstName), LastName(it.lastName)) } ?: FullName.none()
        val reviewer = Reviewer(UserId.ValidUserId(userId), ReviewerName.of(fullName))
        val subject = ReviewSubject(ProductId(Identity(productId)), reviewer)
        return Review(
            ReviewStamp(ReviewId(id), ReviewedAt(reviewedAt)), subject,
            Opinion(Rating(rating), ReviewComment(comment))
        )
    }
}
