package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.review.ReviewId
import org.flexstore.domain.review.usecase.ReviewUseCases
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The moderation of the reviews by the administrators (ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/reviews")
class AdminReviewController(
    private val reviewUseCases: ReviewUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(@AuthenticationPrincipal editor: Jwt, @PathVariable id: String) {
        val actor = actorResolutionService.actorOf(editor)
        reviewUseCases.delete(actor, ReviewId(Identity(id)))
    }
}
