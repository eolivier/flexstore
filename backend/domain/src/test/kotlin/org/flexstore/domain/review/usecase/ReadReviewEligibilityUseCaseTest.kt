package org.flexstore.domain.review.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.Cart
import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.review.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class ReadReviewEligibilityUseCaseTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customerId = UserId.ValidUserId("user-1")
    private val customer = CartOwner.Customer(customerId)
    private val reviewRepository = InMemoryReviewRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val useCase = ReadReviewEligibilityUseCase(reviewRepository, orderRepository)
    private val question = ReviewEligibilityQuestion(customerId, deskLamp.productId)

    private fun storeOrder(status: OrderStatus) {
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        val placement = OrderPlacement(OrderStamp(OrderId.nextId(), Instant.now()),
            Checkout(customer, homeOrderAddresses))
        orderRepository.save(Order.place(placement, cart).withStatus(status))
    }

    private fun storeReview() {
        val stamp = ReviewStamp(ReviewId.nextId(), ReviewedAt(Instant.now()))
        val subject = ReviewSubject(deskLamp.productId, Reviewer(customerId, ReviewerName("Marie D.")))
        reviewRepository.save(Review(stamp, subject, Opinion(Rating(5), ReviewComment("Sturdy"))))
    }

    @ParameterizedTest(name = "an order {0} lets the customer review")
    @CsvSource("PAID", "PROCESSING", "SHIPPED", "DELIVERED")
    fun `a customer who paid for the product should be able to review it`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        val eligibility = useCase.unfold(question)
        // Assert
        assertThat(eligibility).isEqualTo(ReviewEligibility.CAN_REVIEW)
    }

    @ParameterizedTest(name = "an order {0} does not let the customer review")
    @CsvSource("AWAITING_PAYMENT", "CANCELLED", "REFUNDED")
    fun `a customer who did not pay for the product should not be able to review it`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        val eligibility = useCase.unfold(question)
        // Assert
        assertThat(eligibility).isEqualTo(ReviewEligibility.NOT_BOUGHT)
    }

    @Test
    fun `a customer without order should not be able to review the product`() {
        // Act
        val eligibility = useCase.unfold(question)
        // Assert
        assertThat(eligibility).isEqualTo(ReviewEligibility.NOT_BOUGHT)
    }

    @Test
    fun `a customer who already reviewed the product should not review it again`() {
        // Arrange
        storeOrder(OrderStatus.DELIVERED)
        storeReview()
        // Act
        val eligibility = useCase.unfold(question)
        // Assert
        assertThat(eligibility).isEqualTo(ReviewEligibility.ALREADY_REVIEWED)
    }
}
