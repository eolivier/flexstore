package org.flexstore.domain.review

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.model.UserId

// The reviews written by the customers
interface ReviewRepository {

    fun save(review: Review)

    // Newest first
    fun findAllAbout(productId: ProductId): Reviews

    fun findAll(): Reviews

    fun delete(reviewId: ReviewId)

    fun reviewExists(reviewId: ReviewId): Boolean

    fun reviewIsMissing(reviewId: ReviewId) = !reviewExists(reviewId)

    // The customer already gave their opinion on the product
    fun hasReviewed(customer: UserId.ValidUserId, productId: ProductId): Boolean

    fun hasNotReviewed(customer: UserId.ValidUserId, productId: ProductId) = !hasReviewed(customer, productId)
}

// Reviews, in the order given
data class Reviews(private val reviews: List<Review>) {

    constructor(vararg reviews: Review) : this(reviews.toList())

    fun newestFirst() = Reviews(reviews.sortedByDescending { it.reviewedAt.value })

    // The review with this id
    fun of(reviewId: ReviewId): Review = reviews.firstOrNull { it.reviewId == reviewId }
        ?: throw ReviewNotFoundException(ErrorMessage("No review ${reviewId.value}."))

    // What the customers think of each product that has reviews
    fun summaries() = RatingSummaries(
        reviews.groupBy { it.productId }.map { (productId, productReviews) ->
            RatingSummary(
                productId,
                AverageRating.of(productReviews.map { it.rating }),
                ReviewCount(productReviews.size)
            )
        }
    )

    // Read-only view for the adapters (JSON)
    fun asList(): List<Review> = reviews.toList()
}

data class RatingSummaries(private val summaries: List<RatingSummary>) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<RatingSummary> = summaries.toList()
}
