package org.flexstore.domain.review.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.InMemoryReviewRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class DeleteReviewUseCaseTest : WithAssertions {

    private val lamp = ProductId(Identity("lamp"))
    private val customer = UserId.ValidUserId("user-1")
    private val reviewId = ReviewId("review-1")
    private val reviewRepository = InMemoryReviewRepository().apply {
        val stamp = ReviewStamp(reviewId, ReviewedAt(Instant.parse("2026-10-31T10:00:00Z")))
        val subject = ReviewSubject(lamp, Reviewer(customer, ReviewerName("Marie D.")))
        save(Review(stamp, subject, Opinion(Rating(1), ReviewComment("Spam"))))
    }
    private val useCase = DeleteReviewUseCase(reviewRepository)

    @Test
    fun `an administrator should delete a review, which the rating of the product no longer counts`() {
        // Act
        useCase.unfold(reviewId)
        // Assert
        assertThat(reviewRepository.findAll().summaries().asList()).isEmpty()
    }

    @Test
    fun `the customer whose review is deleted should be able to review the product again`() {
        // Act
        useCase.unfold(reviewId)
        // Assert
        assertThat(reviewRepository.hasReviewed(customer, lamp)).isFalse()
    }

    @Test
    fun `a review that does not exist should not be deleted`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(ReviewId("missing")) }
        // Assert
        assertThat(thrown).isInstanceOf(ReviewNotFoundException::class.java)
    }
}
