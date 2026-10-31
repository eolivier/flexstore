package org.flexstore.domain.review

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class ReviewsTest : WithAssertions {

    private val lamp = ProductId(Identity("lamp"))
    private val desk = ProductId(Identity("desk"))
    private val reviewer = Reviewer(UserId.ValidUserId("user-1"), ReviewerName("Marie D."))

    private fun review(id: String, productId: ProductId, rating: Int, at: String): Review {
        val stamp = ReviewStamp(ReviewId(id), ReviewedAt(Instant.parse(at)))
        return Review(stamp, ReviewSubject(productId, reviewer), Opinion(Rating(rating), ReviewComment("ok")))
    }

    @Test
    fun `the reviews should be listed newest first`() {
        // Arrange
        val older = review("older", lamp, 3, "2026-10-01T10:00:00Z")
        val newer = review("newer", lamp, 4, "2026-10-02T10:00:00Z")
        // Act
        val reviews = Reviews(older, newer).newestFirst()
        // Assert
        assertThat(reviews.asList()).containsExactly(newer, older)
    }

    @Test
    fun `the summary of a product should give the average of its ratings with one decimal`() {
        // Arrange
        val reviews = Reviews(
            review("1", lamp, 5, "2026-10-01T10:00:00Z"),
            review("2", lamp, 4, "2026-10-02T10:00:00Z"),
            review("3", lamp, 4, "2026-10-03T10:00:00Z"),
            review("4", desk, 2, "2026-10-03T10:00:00Z")
        )
        // Act
        val summaries = reviews.summaries().asList()
        // Assert
        assertThat(summaries).containsExactlyInAnyOrder(
            RatingSummary(lamp, AverageRating(BigDecimal("4.3")), ReviewCount(3)),
            RatingSummary(desk, AverageRating(BigDecimal("2.0")), ReviewCount(1))
        )
    }

    @Test
    fun `a review that does not exist should not be found`() {
        // Act
        val thrown = catchThrowable { Reviews().of(ReviewId("missing")) }
        // Assert
        assertThat(thrown).isInstanceOf(ReviewNotFoundException::class.java)
    }
}
