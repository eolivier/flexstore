package org.flexstore.domain.review.usecase

import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.product.*
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// A customer reviews a product they bought: a mark from 1 to 5 and a comment, written once per product
class AddReviewUseCase(
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<ReviewSubmission> {

    override fun getPreConditions(): PreConditions<ReviewSubmission> =
        PreConditions(opinionIsValidCondition(), productExistsCondition(), customerBoughtTheProductCondition(),
            reviewIsNotWrittenYetCondition())

    override fun getNominalScenario(): NominalScenario<ReviewSubmission> =
        NominalScenario(Step { submission -> reviewRepository.save(reviewOf(submission)) })

    override fun getPostConditions(): PostConditions<ReviewSubmission> =
        PostConditions(reviewIsStoredCondition())

    private fun reviewOf(submission: ReviewSubmission): Review {
        val draft = submission.draft
        val reviewer = Reviewer(draft.customer, reviewerNameOf(draft))
        return Review(submission.stamp, ReviewSubject(draft.productOpinion.productId, reviewer),
            draft.productOpinion.opinion)
    }

    private fun reviewerNameOf(draft: ReviewDraft): ReviewerName =
        when (val user = userRepository.findById(draft.customer)) {
            is User.DefinedUser -> ReviewerName.of(user.fullName)
            else -> ReviewerName.of(FullName.none())
        }

    private fun opinionIsValidCondition() = PreCondition<ReviewSubmission> { submission ->
        val opinion = submission.draft.productOpinion.opinion
        val reason = when {
            opinion.rating.isInvalid() -> "the rating is from 1 to 5"
            opinion.comment.isBlank() -> "the comment is not empty"
            opinion.comment.isTooLong() -> "the comment is 1000 characters at most"
            else -> null
        }
        if (reason != null) throw InvalidReviewException(ErrorMessage("The review is not valid: $reason."))
    }

    private fun productExistsCondition() = PreCondition<ReviewSubmission> { submission ->
        val productId = submission.draft.productOpinion.productId
        if (productRepository.productIsMissing(productId)) {
            throw ProductNotFoundException(
                ErrorMessage("The product ${productId.id.value} does not exist.")
            )
        }
    }

    private fun customerBoughtTheProductCondition() = PreCondition<ReviewSubmission> { submission ->
        val draft = submission.draft
        val orders = orderRepository.findAllBy(draft.customer)
        if (!orders.hasPaidFor(draft.productOpinion.productId)) {
            throw ReviewNotAllowedException(ErrorMessage("Only a customer who bought a product can review it."))
        }
    }

    private fun reviewIsNotWrittenYetCondition() = PreCondition<ReviewSubmission> { submission ->
        val draft = submission.draft
        if (reviewRepository.hasReviewed(draft.customer, draft.productOpinion.productId)) {
            throw ReviewAlreadyWrittenException(ErrorMessage("The customer already reviewed this product."))
        }
    }

    private fun reviewIsStoredCondition() = PostCondition<ReviewSubmission> { submission ->
        if (!reviewRepository.reviewExists(submission.stamp.reviewId)) {
            throw ReviewAdditionFailed(ErrorMessage("The review ${submission.stamp.reviewId.value} was not stored."))
        }
    }
}
