package org.flexstore.domain.review

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.FullName
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.NominalException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

data class ReviewId(val value: String) {
    companion object {
        fun nextId() = ReviewId(Identity.nextId().value)
    }
}

// The mark a customer gives to a product, from 1 to 5 stars
data class Rating(val value: Int) {

    fun isValid() = value in LOWEST..HIGHEST

    fun isInvalid() = !isValid()

    companion object {
        private const val LOWEST = 1
        private const val HIGHEST = 5
    }
}

// What a customer says about a product, 1000 characters at most
data class ReviewComment(val value: String) {

    fun isBlank() = value.isBlank()

    fun isTooLong() = value.length > MAXIMUM_LENGTH

    companion object {
        private const val MAXIMUM_LENGTH = 1000
    }
}

// The opinion of a customer on a product: its mark and its comment
data class Opinion(val rating: Rating, val comment: ReviewComment) {

    fun isInvalid() = rating.isInvalid() || comment.isBlank() || comment.isTooLong()
}

// How a reviewer is named under their review: the first name and the initial of the last name (Marie D.)
data class ReviewerName(val value: String) {
    companion object {
        private val ANONYMOUS = ReviewerName("A customer")

        fun of(fullName: FullName): ReviewerName {
            val firstName = fullName.firstName.value.trim()
            val lastNameInitial = fullName.lastName.value.trim().take(1).uppercase()
            return when {
                firstName.isEmpty() -> ANONYMOUS
                lastNameInitial.isEmpty() -> ReviewerName(firstName)
                else -> ReviewerName("$firstName $lastNameInitial.")
            }
        }
    }
}

// The customer who wrote a review
data class Reviewer(val userId: UserId.ValidUserId, val name: ReviewerName)

// Who gives an opinion on which product
data class ReviewSubject(val productId: ProductId, val reviewer: Reviewer)

data class ReviewedAt(val value: Instant) {
    companion object {
        fun now() = ReviewedAt(InstantProvider.now())
    }
}

// The id of a new review (chosen by the caller, which can then read it) and the moment it is written
data class ReviewStamp(val reviewId: ReviewId, val reviewedAt: ReviewedAt)

// A review: the opinion of a customer on a product they bought, written once
data class Review(val stamp: ReviewStamp, val subject: ReviewSubject, val opinion: Opinion) {

    val reviewId get() = stamp.reviewId

    val reviewedAt get() = stamp.reviewedAt

    val productId get() = subject.productId

    val reviewer get() = subject.reviewer

    val rating get() = opinion.rating

    val comment get() = opinion.comment

    fun isAbout(productId: ProductId) = this.productId == productId

    fun isWrittenBy(customer: UserId.ValidUserId) = reviewer.userId == customer
}

// What a customer sends: the product they review and their opinion
data class ProductOpinion(val productId: ProductId, val opinion: Opinion)

// A review to write: by a customer, whose name is read when the review is written
data class ReviewDraft(val customer: UserId.ValidUserId, val productOpinion: ProductOpinion)

// Whether a customer may review a product: they did not review it yet, and bought (and paid for) it
enum class ReviewEligibility {
    CAN_REVIEW, NOT_BOUGHT, ALREADY_REVIEWED
}

// May this customer review this product?
data class ReviewEligibilityQuestion(val customer: UserId.ValidUserId, val productId: ProductId)

// A review to add, with its id and the moment it is written
data class ReviewSubmission(val stamp: ReviewStamp, val draft: ReviewDraft)

// The average of the ratings of a product, with one decimal
data class AverageRating(val value: BigDecimal) {
    companion object {
        fun of(ratings: List<Rating>): AverageRating {
            val sum = ratings.sumOf { it.value }
            return AverageRating(BigDecimal(sum).divide(BigDecimal(ratings.size), 1, RoundingMode.HALF_UP))
        }
    }
}

data class ReviewCount(val value: Int)

// What the customers think of a product: the average of its ratings and how many reviews give them
data class RatingSummary(val productId: ProductId, val average: AverageRating, val reviewCount: ReviewCount)

data class InvalidReviewException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ReviewNotAllowedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ReviewAlreadyWrittenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class ReviewNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class ReviewAdditionFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
