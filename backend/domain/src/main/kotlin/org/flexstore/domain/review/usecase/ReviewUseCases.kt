package org.flexstore.domain.review.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*

// The use cases of the reviews of the products
class ReviewUseCases(
    private val addReviewUseCase: AddReviewUseCase,
    private val listProductReviewsUseCase: ListProductReviewsUseCase,
    private val listRatingSummariesUseCase: ListRatingSummariesUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase
) {
    fun add(submission: ReviewSubmission) = addReviewUseCase.unfold(submission)

    fun getAbout(productId: ProductId) = listProductReviewsUseCase.unfold(productId)

    fun getSummaries() = listRatingSummariesUseCase.unfold(Unit)

    fun delete(reviewId: ReviewId) = deleteReviewUseCase.unfold(reviewId)
}
