package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.review.usecase.ReviewUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.rest.json.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The reviews of the products: read by everybody, written by a signed-in customer who bought the product
@Tag(name = "Reviews")
@RestController
@RequestMapping("/api")
class ReviewController(private val reviewUseCases: ReviewUseCases) {

    @Operation(summary = "Retrieve the reviews of a product, newest first")
    @GetMapping("/products/{id}/reviews")
    fun getReviews(@PathVariable id: String): List<JsonReview> =
        reviewUseCases.getAbout(productId(id)).asList().map { JsonReview(it) }

    @Operation(summary = "Review a product bought by the signed-in customer: a rating from 1 to 5 and a comment")
    @PostMapping("/products/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun addReview(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String,
        @RequestBody newReview: JsonNewReview
    ): JsonReview {
        val productId = productId(id)
        val stamp = ReviewStamp(ReviewId.nextId(), ReviewedAt.now())
        val customer = UserId.ValidUserId(jwt.subject)
        val productOpinion = ProductOpinion(productId, newReview.toOpinion())
        reviewUseCases.add(ReviewSubmission(stamp, ReviewDraft(customer, productOpinion)))
        return JsonReview(reviewUseCases.getAbout(productId).of(stamp.reviewId))
    }

    @Operation(summary = "Tell whether the signed-in customer may review a product: bought, not reviewed yet")
    @GetMapping("/products/{id}/review-eligibility")
    fun getReviewEligibility(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String
    ): JsonReviewEligibility {
        val question = ReviewEligibilityQuestion(UserId.ValidUserId(jwt.subject), productId(id))
        return JsonReviewEligibility(reviewUseCases.eligibilityOf(question).name)
    }

    @Operation(summary = "Retrieve what the customers think of each product that has reviews")
    @GetMapping("/product-ratings")
    fun getRatings(): List<JsonRatingSummary> =
        reviewUseCases.getSummaries().asList().map { JsonRatingSummary(it) }

    private fun productId(id: String) = ProductId(Identity(id))
}
