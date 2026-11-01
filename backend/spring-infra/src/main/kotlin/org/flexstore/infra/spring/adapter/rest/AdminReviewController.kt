package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.review.ReviewId
import org.flexstore.domain.review.usecase.ReviewUseCases
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// The moderation of the reviews by the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/reviews")
class AdminReviewController(private val reviewUseCases: ReviewUseCases) {

    @Operation(summary = "Delete a review: the rating of its product no longer counts it")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(@PathVariable id: String) = reviewUseCases.delete(ReviewId(id))
}
