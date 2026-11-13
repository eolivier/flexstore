package org.flexstore.domain.review.usecase

import org.flexstore.domain.review.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator deletes a review (a comment that has no place in the shop): the rating of the product no
// longer counts it, and the customer may review the product again
class DeleteReviewUseCase(
    private val reviewRepository: ReviewRepository
) : UseCase<ReviewDeletion> {

    override fun getPreConditions(): PreConditions<ReviewDeletion> =
        PreConditions(editorIsAdministratorCondition(), reviewExistsCondition())

    override fun getNominalScenario(): NominalScenario<ReviewDeletion> =
        NominalScenario(Step { (_, reviewId) -> reviewRepository.delete(reviewId) })

    override fun getPostConditions(): PostConditions<ReviewDeletion> = PostConditions(reviewIsGoneCondition())

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<ReviewDeletion> { request ->
        if (request.editor.isNotAdmin()) {
            throw ReviewModerationForbiddenException(ErrorMessage("Only an administrator can delete a review."))
        }
    }

    private fun reviewExistsCondition() = PreCondition<ReviewDeletion> { (_, reviewId) ->
        if (reviewRepository.reviewIsMissing(reviewId)) {
            throw ReviewNotFoundException(ErrorMessage("The review ${reviewId} does not exist."))
        }
    }

    private fun reviewIsGoneCondition() = PostCondition<ReviewDeletion> { (_, reviewId) ->
        if (reviewRepository.reviewExists(reviewId)) {
            throw ReviewDeletionFailed(ErrorMessage("The review ${reviewId} was not deleted."))
        }
    }
}
