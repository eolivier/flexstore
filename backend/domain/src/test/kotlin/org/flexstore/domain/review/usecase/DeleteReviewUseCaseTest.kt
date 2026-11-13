package org.flexstore.domain.review.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.InMemoryReviewRepository
import org.junit.jupiter.api.Test
import java.time.Instant
import org.flexstore.domain.user.*
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor

class DeleteReviewUseCaseTest : WithAssertions {

    private val lamp = ProductId(Identity("lamp"))
    private val customer = UserId.ValidUserId("user-1")
    private val reviewId = ReviewId(Identity("review-1"))
    private val reviewRepository = InMemoryReviewRepository().apply {
        val stamp = ReviewStamp(reviewId, ReviewedAt(Instant.parse("2026-10-31T10:00:00Z")))
        val subject = ReviewSubject(lamp, Reviewer(customer, ReviewerName("Marie D.")))
        save(Review(stamp, subject, Opinion(Rating(1), ReviewComment("Spam"))))
    }
    private val administrator = testAdministratorActor
    private val useCase = DeleteReviewUseCase(reviewRepository)

    @Test
    fun `an administrator should delete a review, which the rating of the product no longer counts`() {
        // Act
        useCase.unfold(ReviewDeletion(administrator, reviewId))
        // Assert
        assertThat(reviewRepository.findAll().summaries().asList()).isEmpty()
    }

    @Test
    fun `the customer whose review is deleted should be able to review the product again`() {
        // Act
        useCase.unfold(ReviewDeletion(administrator, reviewId))
        // Assert
        assertThat(reviewRepository.hasReviewed(customer, lamp)).isFalse()
    }

    @Test
    fun `a review that does not exist should not be deleted`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(ReviewDeletion(administrator, ReviewId(Identity("missing")))) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ReviewNotFoundException::class.java)
    }

    @Test
    fun `a customer should not delete a review`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(ReviewDeletion(testCustomerActor, reviewId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ReviewModerationForbiddenException::class.java)
        assertThat(reviewRepository.reviewExists(reviewId)).isTrue()
    }
}
