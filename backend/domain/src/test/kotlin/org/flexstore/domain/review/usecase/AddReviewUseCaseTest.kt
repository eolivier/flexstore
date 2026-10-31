package org.flexstore.domain.review.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.Cart
import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class AddReviewUseCaseTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customerId = UserId.ValidUserId("user-1")
    private val customer = CartOwner.Customer(customerId)
    private val reviewRepository = InMemoryReviewRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val userRepository = InMemoryUserRepository().apply {
        save(User.DefinedUser(customerId, FullName(FirstName("Marie"), LastName("Durand")), Email("m@d.fr"),
            HashedPassword("hash")))
    }
    private val useCase = AddReviewUseCase(reviewRepository, orderRepository, productRepository, userRepository)
    private val sturdy = Opinion(Rating(5), ReviewComment("Sturdy and bright"))

    private fun storeOrder(status: OrderStatus) {
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        val placement = OrderPlacement(OrderStamp(OrderId.nextId(), Instant.now()),
            Checkout(customer, homeOrderAddresses))
        orderRepository.save(Order.place(placement, cart).withStatus(status))
    }

    private fun submission(opinion: Opinion, productId: ProductId = deskLamp.productId): ReviewSubmission {
        val stamp = ReviewStamp(ReviewId("review-1"), ReviewedAt(Instant.parse("2026-10-31T10:00:00Z")))
        return ReviewSubmission(stamp, ReviewDraft(customerId, ProductOpinion(productId, opinion)))
    }

    @Test
    fun `a customer who bought a product should review it under their name`() {
        // Arrange
        storeOrder(OrderStatus.DELIVERED)
        // Act
        useCase.unfold(submission(sturdy))
        // Assert
        val review = reviewRepository.findAllAbout(deskLamp.productId).of(ReviewId("review-1"))
        assertThat(review.reviewer.name).isEqualTo(ReviewerName("Marie D."))
        assertThat(review.opinion).isEqualTo(sturdy)
    }

    @Test
    fun `a customer who did not buy the product should be refused`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(submission(sturdy)) }
        // Assert
        assertThat(thrown).isInstanceOf(ReviewNotAllowedException::class.java)
    }

    @ParameterizedTest(name = "an order {0} does not let the customer review")
    @CsvSource("AWAITING_PAYMENT", "CANCELLED", "REFUNDED")
    fun `an order not paid should not let the customer review its product`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        val thrown = catchThrowable { useCase.unfold(submission(sturdy)) }
        // Assert
        assertThat(thrown).isInstanceOf(ReviewNotAllowedException::class.java)
    }

    @Test
    fun `a second review of the same product by the same customer should be refused`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        useCase.unfold(submission(sturdy))
        // Act
        val thrown = catchThrowable { useCase.unfold(submission(sturdy)) }
        // Assert
        assertThat(thrown).isInstanceOf(ReviewAlreadyWrittenException::class.java)
    }

    @ParameterizedTest(name = "a rating of {0} with the comment ''{1}'' is refused")
    @CsvSource("0, fine", "6, fine", "3, ' '")
    fun `an invalid opinion should be refused`(rating: Int, comment: String) {
        // Arrange
        storeOrder(OrderStatus.PAID)
        // Act
        val opinion = Opinion(Rating(rating), ReviewComment(comment))
        val thrown = catchThrowable { useCase.unfold(submission(opinion)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidReviewException::class.java)
    }

    @Test
    fun `a comment of more than 1000 characters should be refused`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        val tooLong = Opinion(Rating(4), ReviewComment("a".repeat(1001)))
        // Act
        val thrown = catchThrowable { useCase.unfold(submission(tooLong)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidReviewException::class.java)
    }

    @Test
    fun `a product that does not exist should be refused`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        // Act
        val thrown = catchThrowable { useCase.unfold(submission(sturdy, ProductId(Identity("unknown")))) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
    }
}
