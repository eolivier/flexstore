package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.review.usecase.ReviewUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.rest.json.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The reviews of the products: read by everybody, written by a signed-in customer who bought the product
@RestController
@RequestMapping("/api")
class ReviewController(
    private val reviewUseCases: ReviewUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/products/{id}/reviews")
    fun getReviews(@PathVariable id: String): List<JsonReview> =
        reviewUseCases.getAbout(actorResolutionService.visitor(), productId(id)).asList().map { JsonReview(it) }

    @PostMapping("/products/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun addReview(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String,
        @RequestBody newReview: JsonNewReview
    ): JsonReview {
        val productId = productId(id)
        val stamp = ReviewStamp(ReviewId.nextId(), ReviewedAt.now())
        val customer = actorResolutionService.actorOf(jwt)
        val productOpinion = ProductOpinion(productId, newReview.toOpinion())
        reviewUseCases.add(ReviewSubmission(stamp, ReviewDraft(customer, productOpinion)))
        return JsonReview(reviewUseCases.getAbout(customer, productId).of(stamp.reviewId))
    }

    @GetMapping("/products/{id}/review-eligibility")
    fun getReviewEligibility(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String
    ): JsonReviewEligibility {
        val customer = actorResolutionService.actorOf(jwt)
        val question = ReviewEligibilityQuestion(customer, productId(id))
        return JsonReviewEligibility(reviewUseCases.eligibilityOf(question).name)
    }

    @GetMapping("/product-ratings")
    fun getRatings(): List<JsonRatingSummary> =
        reviewUseCases.getSummaries(actorResolutionService.visitor()).asList().map { JsonRatingSummary(it) }

    private fun productId(id: String) = ProductId(Identity(id))
}
