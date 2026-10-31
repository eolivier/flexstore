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

    fun existsByUser_IdAndProductId(userId: String, productId: String): Boolean
}

// The reviews, kept in PostgreSQL; the reviewer is named from the account of the customer when it is read
@Repository
@Transactional
class PostgresReviewRepositoryAdapter(
    private val jpaRepo: JpaProductReviewRepository,
    private val userRepo: JpaUserRepository
) : ReviewRepository {

    override fun save(review: Review) {
        jpaRepo.save(review.toEntity())
    }

    override fun delete(reviewId: ReviewId) {
        jpaRepo.deleteById(reviewId.value)
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
        jpaRepo.existsByUser_IdAndProductId(customer.value, productId.id.value)

    // The reviewer is the row of the customer, so that the review read back in the same request is named
    private fun Review.toEntity() = ProductReviewEntity(
        reviewId.value, productId.id.value, userRepo.getReferenceById(reviewer.userId.value), rating.value,
        comment.value, reviewedAt.value
    )

    private fun ProductReviewEntity.toDomain(): Review {
        val fullName = FullName(FirstName(user.firstName), LastName(user.lastName))
        val reviewer = Reviewer(UserId.ValidUserId(user.id), ReviewerName.of(fullName))
        val subject = ReviewSubject(ProductId(Identity(productId)), reviewer)
        return Review(
            ReviewStamp(ReviewId(id), ReviewedAt(reviewedAt)), subject,
            Opinion(Rating(rating), ReviewComment(comment))
        )
    }
}
