package org.flexstore.domain.review.usecase

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.review.*
import org.ucop.domain.entity.Actor

// The use cases of the reviews of the products
class ReviewUseCases(
    private val addReviewUseCase: AddReviewUseCase,
    private val listProductReviewsUseCase: ListProductReviewsUseCase,
    private val listRatingSummariesUseCase: ListRatingSummariesUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase,
    private val readReviewEligibilityUseCase: ReadReviewEligibilityUseCase
) {
    fun add(submission: ReviewSubmission) = addReviewUseCase.unfold(submission)

    fun getAbout(reader: Actor, productId: ProductId) =
        listProductReviewsUseCase.unfold(ProductReviewsReading(reader, productId))

    fun getSummaries(reader: Actor) = listRatingSummariesUseCase.unfold(reader)

    fun delete(editor: Actor, reviewId: ReviewId) = deleteReviewUseCase.unfold(ReviewDeletion(editor, reviewId))

    fun eligibilityOf(question: ReviewEligibilityQuestion) = readReviewEligibilityUseCase.unfold(question)
}
