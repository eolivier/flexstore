package org.flexstore.domain.restock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.*
import org.flexstore.domain.email.model.RestockEmail
import org.flexstore.domain.product.model.*
import org.flexstore.domain.restock.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.Stock
import org.flexstore.domain.stock.StockQuantity
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.Instant

class NotifyRestockUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val userId = UserId.ValidUserId("user-1")
    private val restockSubscription = RestockSubscription(
        RestockSubscriptionId.nextId(), userId, deskLamp.productId, SubscribedAt(Instant.parse("2026-11-05T10:00:00Z"))
    )
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val stockRepository = InMemoryStockRepository().apply {
        save(Stock(deskLamp.productId, StockQuantity(10)))
    }
    private val restockSubscriptionRepository = InMemoryRestockSubscriptionRepository().apply {
        save(restockSubscription)
    }
    private val userRepository = customersWithIds("user-1")
    private val emailRepository = InMemoryEmailRepository()
    private val useCase = NotifyRestockUseCase(
        restockSubscriptionRepository, productRepository, stockRepository, userRepository, emailRepository
    )
    private val systemActor = SystemActor()
    private val actorName = Name("Jane Doe")

    @Test
    fun `should send the customer an email about the product back in stock`() {
        // Act
        useCase.unfold(RestockNotifying(systemActor, restockSubscription))
        // Assert
        val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
        val restockEmail = RestockEmail(Email("user-1@flexstore.fr"), janeDoe, deskLamp)
        assertThat(emailRepository.sentRestockEmails()).containsExactly(restockEmail)
    }

    @Test
    fun `should end the subscription once the customer is notified`() {
        // Act
        useCase.unfold(RestockNotifying(systemActor, restockSubscription))
        // Assert
        assertThat(restockSubscriptionRepository.contains(userId, deskLamp.productId)).isFalse()
    }

    @Test
    fun `should not notify while the product is out of stock`() {
        // Arrange
        stockRepository.save(Stock(deskLamp.productId, StockQuantity(0)))
        // Act
        val thrown = catchThrowable { useCase.unfold(RestockNotifying(systemActor, restockSubscription)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(RestockNotificationRefusedException::class.java)
        assertThat(emailRepository.sentRestockEmails()).isEmpty()
        assertThat(restockSubscriptionRepository.contains(userId, deskLamp.productId)).isTrue()
    }

    @ParameterizedTest(name = "a customer whose account is {0} is not notified")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `should not notify a customer whose account cannot act`(status: AccountStatus) {
        // Arrange
        val blockedSubscription = restockSubscription.copy(customer = blockedCustomerId)
        val blockedUsers = InMemoryUserRepository().apply { save(customerWithStatus(status)) }
        restockSubscriptionRepository.save(blockedSubscription)
        val blockedUseCase = NotifyRestockUseCase(
            restockSubscriptionRepository, productRepository, stockRepository, blockedUsers, emailRepository
        )
        // Act
        val thrown = catchThrowable { blockedUseCase.unfold(RestockNotifying(systemActor, blockedSubscription)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
        assertThat(emailRepository.sentRestockEmails()).isEmpty()
    }

    @Test
    fun `an administrator should notify a customer`() {
        // Arrange
        val administrator = Admin(userId, actorName, AccountStatus.ACTIVE)
        // Act
        useCase.unfold(RestockNotifying(administrator, restockSubscription))
        // Assert
        assertThat(emailRepository.sentRestockEmails()).hasSize(1)
    }

    @Test
    fun `a customer should not notify a customer`() {
        // Arrange
        val customer = Customer(userId, actorName, AccountStatus.ACTIVE)
        // Act
        val thrown = catchThrowable { useCase.unfold(RestockNotifying(customer, restockSubscription)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(RestockNotificationForbiddenException::class.java)
        assertThat(emailRepository.sentRestockEmails()).isEmpty()
    }

    @Test
    fun `a visitor should not notify a customer`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(RestockNotifying(Visitor(), restockSubscription)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(RestockNotificationForbiddenException::class.java)
        assertThat(restockSubscriptionRepository.contains(userId, deskLamp.productId)).isTrue()
    }
}
