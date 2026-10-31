package org.flexstore.domain.review.usecase

import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator deletes a review (a comment that has no place in the shop): the rating of the product no
// longer counts it, and the customer may review the product again
class DeleteReviewUseCase(
    private val reviewRepository: ReviewRepository
) : UseCase<ReviewId> {

    override fun getPreConditions(): PreConditions<ReviewId> = PreConditions(reviewExistsCondition())

    override fun getNominalScenario(): NominalScenario<ReviewId> =
        NominalScenario(Step { reviewId -> reviewRepository.delete(reviewId) })

    override fun getPostConditions(): PostConditions<ReviewId> = PostConditions(reviewIsGoneCondition())

    private fun reviewExistsCondition() = PreCondition<ReviewId> { reviewId ->
        if (reviewRepository.reviewIsMissing(reviewId)) {
            throw ReviewNotFoundException(ErrorMessage("The review ${reviewId.value} does not exist."))
        }
    }

    private fun reviewIsGoneCondition() = PostCondition<ReviewId> { reviewId ->
        if (reviewRepository.reviewExists(reviewId)) {
            throw ReviewDeletionFailed(ErrorMessage("The review ${reviewId.value} was not deleted."))
        }
    }
}
