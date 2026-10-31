package org.flexstore.infra.repository

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.user.model.UserId

// The reviews in memory: for the tests
class InMemoryReviewRepository : ReviewRepository {

    private val reviews = mutableListOf<Review>()

    override fun save(review: Review) {
        reviews.add(review)
    }

    override fun findAllAbout(productId: ProductId) = Reviews(reviews.filter { it.isAbout(productId) })

    override fun findAll() = Reviews(reviews.toList())

    override fun reviewExists(reviewId: ReviewId) = reviews.any { it.reviewId == reviewId }

    override fun hasReviewed(customer: UserId.ValidUserId, productId: ProductId) =
        reviews.any { it.isAbout(productId) && it.isWrittenBy(customer) }
}
