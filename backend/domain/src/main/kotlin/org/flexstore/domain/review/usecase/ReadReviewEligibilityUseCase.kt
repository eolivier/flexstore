package org.flexstore.domain.review.usecase

import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.review.*
import org.ucop.domain.entity.*

// Whether a signed-in customer may review a product, so that the form is only offered to the customers who can
// send it: a review already written first, then a product not bought (no paid order with it)
class ReadReviewEligibilityUseCase(
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository
) : QueryUseCase<ReviewEligibilityQuestion, ReviewEligibility> {

    override fun getPreConditions(): PreConditions<ReviewEligibilityQuestion> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<ReviewEligibilityQuestion, ReviewEligibility> { question ->
        val customer = question.customer
        val productId = question.productId
        when {
            reviewRepository.hasReviewed(customer, productId) -> ReviewEligibility.ALREADY_REVIEWED
            orderRepository.findAllBy(customer).hasPaidFor(productId) -> ReviewEligibility.CAN_REVIEW
            else -> ReviewEligibility.NOT_BOUGHT
        }
    }

    override fun getPostConditions(): PostConditions<ReviewEligibility> = PostConditions.none()
}
