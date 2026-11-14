package org.flexstore.domain.restock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryRestockSubscriptionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class UnsubscribeFromRestockUseCaseTest : WithAssertions {

    private val deskLamp = ProductId(Identity("desk-lamp"))
    private val customer = UserId.ValidUserId("user-1")
    private val otherCustomer = UserId.ValidUserId("user-2")
    private val subscribedAt = SubscribedAt(Instant.parse("2026-11-05T10:00:00Z"))
    private val restockSubscriptionRepository = InMemoryRestockSubscriptionRepository().apply {
        save(RestockSubscription(RestockSubscriptionId.nextId(), customer, deskLamp, subscribedAt))
        save(RestockSubscription(RestockSubscriptionId.nextId(), otherCustomer, deskLamp, subscribedAt))
    }
    private val userRepository = customersWithIds(customer.value, otherCustomer.value)
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val useCase = UnsubscribeFromRestockUseCase(restockSubscriptionRepository)

    @Test
    fun `should end the subscription of the customer`() {
        // Arrange
        val unsubscribing = RestockUnsubscribing(actorResolutionService.actorOf(customer), deskLamp)
        // Act
        useCase.unfold(unsubscribing)
        // Assert
        assertThat(restockSubscriptionRepository.contains(customer, deskLamp)).isFalse()
    }

    @Test
    fun `should leave the subscriptions of the other customers`() {
        // Arrange
        val unsubscribing = RestockUnsubscribing(actorResolutionService.actorOf(customer), deskLamp)
        // Act
        useCase.unfold(unsubscribing)
        // Assert
        assertThat(restockSubscriptionRepository.contains(otherCustomer, deskLamp)).isTrue()
    }

    @Test
    fun `unsubscribing twice should leave the subscriptions as they are`() {
        // Arrange
        val unsubscribing = RestockUnsubscribing(actorResolutionService.actorOf(customer), deskLamp)
        useCase.unfold(unsubscribing)
        // Act
        useCase.unfold(unsubscribing)
        // Assert
        assertThat(restockSubscriptionRepository.findAll().asList()).hasSize(1)
    }

    @Test
    fun `an unknown customer should not unsubscribe`() {
        // Arrange
        val unsubscribing = RestockUnsubscribing(actorResolutionService.actorOf(unknownCustomerId), deskLamp)
        // Act
        val thrown = catchThrowable { useCase.unfold(unsubscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest(name = "a customer whose account is {0} should not unsubscribe")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not unsubscribe`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val unsubscribing = RestockUnsubscribing(actorResolutionService.actorOf(blockedCustomerId), deskLamp)
        // Act
        val thrown = catchThrowable { useCase.unfold(unsubscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
