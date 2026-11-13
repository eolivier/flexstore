package org.flexstore.domain.review.usecase

import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.review.*
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer reviews a product they bought: a mark from 1 to 5 and a comment, written once per product
class AddReviewUseCase(
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<ReviewSubmission> {

    override fun getPreConditions(): PreConditions<ReviewSubmission> =
        PreConditions(customerCanActCondition(), opinionIsValidCondition(), productExistsCondition(),
            customerBoughtTheProductCondition(), reviewIsNotWrittenYetCondition())

    override fun getNominalScenario(): NominalScenario<ReviewSubmission> =
        NominalScenario(Step { submission -> reviewRepository.save(reviewOf(submission)) })

    override fun getPostConditions(): PostConditions<ReviewSubmission> =
        PostConditions(reviewIsStoredCondition())

    private fun reviewOf(submission: ReviewSubmission): Review {
        val draft = submission.draft
        val reviewer = Reviewer(draft.customerId(), reviewerNameOf(draft))
        return Review(submission.stamp, ReviewSubject(draft.productOpinion.productId, reviewer),
            draft.productOpinion.opinion)
    }

    private fun reviewerNameOf(draft: ReviewDraft): ReviewerName =
        when (val user = userRepository.findById(draft.customerId())) {
            is User.DefinedUser -> ReviewerName.of(user.fullName)
            else -> ReviewerName.of(FullName.none())
        }

    // The customer, resolved before the use case runs, must be a customer or an admin who can act: an account
    // neither suspended, locked nor disabled
    private fun customerCanActCondition() = PreCondition<ReviewSubmission> { submission ->
        submission.draft.customer.verifyCanAct()
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
            val message = ErrorMessage("The product ${productId.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun customerBoughtTheProductCondition() = PreCondition<ReviewSubmission> { submission ->
        val draft = submission.draft
        val orders = orderRepository.findAllBy(draft.customerId())
        if (orders.hasNotPaidFor(draft.productOpinion.productId)) {
            throw ReviewNotAllowedException(ErrorMessage("Only a customer who bought a product can review it."))
        }
    }

    private fun reviewIsNotWrittenYetCondition() = PreCondition<ReviewSubmission> { submission ->
        val draft = submission.draft
        if (reviewRepository.hasReviewed(draft.customerId(), draft.productOpinion.productId)) {
            throw ReviewAlreadyWrittenException(ErrorMessage("The customer already reviewed this product."))
        }
    }

    private fun reviewIsStoredCondition() = PostCondition<ReviewSubmission> { submission ->
        if (reviewRepository.reviewIsMissing(submission.stamp.reviewId)) {
            val message = ErrorMessage("The review ${submission.stamp.reviewId.value} was not stored.")
            throw ReviewAdditionFailed(message)
        }
    }
}
