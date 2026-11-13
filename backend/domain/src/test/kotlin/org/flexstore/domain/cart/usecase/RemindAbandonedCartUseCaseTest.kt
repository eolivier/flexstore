package org.flexstore.domain.cart.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.email.model.AbandonedCartReminderEmail
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal

class RemindAbandonedCartUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val userRepository = customersWithIds("user-1")
    private val emailRepository = InMemoryEmailRepository()
    private val useCase = RemindAbandonedCartUseCase(cartRepository, userRepository, emailRepository)
    private val systemActor = SystemActor()
    private val userId = UserId.ValidUserId("user-1")
    private val actorName = Name("Jane Doe")

    @Test
    fun `should send the customer an email with their cart`() {
        // Arrange
        val cart = cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(AbandonedCartReminding(systemActor, customer))
        // Assert
        val jane = FullName(FirstName("Jane"), LastName("Doe"))
        val reminder = AbandonedCartReminderEmail(Email("user-1@flexstore.fr"), jane, cart)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).containsExactly(reminder)
    }

    @Test
    fun `should mark the cart reminded`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(AbandonedCartReminding(systemActor, customer))
        // Assert
        assertThat(cartRepository.isReminded(customer)).isTrue()
    }

    @Test
    fun `should not remind a customer of an empty cart`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AbandonedCartReminding(systemActor, customer)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AbandonedCartReminderRefusedException::class.java)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @Test
    fun `should not remind an unknown customer`() {
        // Arrange
        val unknownCustomer = CartOwner.Customer(unknownCustomerId)
        cartRepository.save(Cart(unknownCustomer).add(deskLamp, Quantity(1)))
        // Act
        val thrown = catchThrowable { useCase.unfold(AbandonedCartReminding(systemActor, unknownCustomer)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @ParameterizedTest(name = "a customer whose account is {0} is not reminded")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `should not remind a customer whose account cannot act`(status: AccountStatus) {
        // Arrange
        val blockedCustomer = CartOwner.Customer(blockedCustomerId)
        val blockedUsers = InMemoryUserRepository().apply { save(customerWithStatus(status)) }
        val blockedUseCase = RemindAbandonedCartUseCase(cartRepository, blockedUsers, emailRepository)
        cartRepository.save(Cart(blockedCustomer).add(deskLamp, Quantity(1)))
        // Act
        val reminding = AbandonedCartReminding(systemActor, blockedCustomer)
        val thrown = catchThrowable { blockedUseCase.unfold(reminding) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @Test
    fun `an administrator should remind a customer of their cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(AbandonedCartReminding(Admin(userId, actorName, AccountStatus.ACTIVE), customer))
        // Assert
        assertThat(cartRepository.isReminded(customer)).isTrue()
    }

    @Test
    fun `a customer should not remind a customer of their cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val reminding = AbandonedCartReminding(Customer(userId, actorName, AccountStatus.ACTIVE), customer)
        val thrown = catchThrowable { useCase.unfold(reminding) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AbandonedCartForbiddenException::class.java)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @Test
    fun `a visitor should not remind a customer of their cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val thrown = catchThrowable { useCase.unfold(AbandonedCartReminding(Visitor(), customer)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AbandonedCartForbiddenException::class.java)
        assertThat(cartRepository.isReminded(customer)).isFalse()
    }
}
