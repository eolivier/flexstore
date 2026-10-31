package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import org.flexstore.domain.review.*

// A review of a product: the rating (1 to 5) and the comment of a customer, named by their first name and the
// initial of their last name
data class JsonReview(
    @JsonProperty("id") val id: String,
    @JsonProperty("productId") val productId: String,
    @JsonProperty("reviewerName") val reviewerName: String,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("comment") val comment: String,
    @JsonProperty("reviewedAt") val reviewedAt: String
) {
    constructor(review: Review) : this(
        review.reviewId.value,
        review.productId.id.value,
        review.reviewer.name.value,
        review.rating.value,
        review.comment.value,
        review.reviewedAt.value.toString()
    )
}

// The opinion of a customer on a product they bought
data class JsonNewReview @JsonCreator constructor(
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("comment") val comment: String
) {
    fun toOpinion() = Opinion(Rating(rating), ReviewComment(comment.trim()))
}

// What the customers think of a product: the average of the ratings, from 1 to 5, and the number of reviews
data class JsonRatingSummary(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("average") val average: BigDecimal,
    @JsonProperty("reviewCount") val reviewCount: Int
) {
    constructor(summary: RatingSummary) : this(
        summary.productId.id.value, summary.average.value, summary.reviewCount.value
    )
}
