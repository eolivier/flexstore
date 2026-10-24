package org.flexstore.domain.order.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class SendOrderConfirmationEmailUseCaseTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = User.DefinedUser(UserId.ValidUserId("user-1"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password123"))
    private val owner = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val order = Order.place(OrderPlacement(OrderId("order-1"), owner, Instant.parse("2026-10-24T10:00:00Z"), homeAddresses), Cart(owner).add(lamp, Quantity(2)))
    private val userRepository = InMemoryUserRepository().apply { save(customer) }
    private val emailRepository = InMemoryEmailRepository()

    @ParameterizedTest(name = "order confirmation email {0} -> {1} email(s) sent")
    @CsvSource("ENABLED, 1", "DISABLED, 0")
    fun `should send the confirmation email only when the shop settings enable it`(activation: Activation, sentCount: Int) {
        // Arrange
        val settingsRepository = InMemoryShopSettingsRepository(ShopSettings(orderConfirmationEmail = activation))
        val useCase = SendOrderConfirmationEmailUseCase(settingsRepository, userRepository, emailRepository)
        // Act
        useCase.unfold(order)
        // Assert
        assertThat(emailRepository.sentEmails()).hasSize(sentCount)
    }

    @Test
    fun `should send the email to the customer, with the order`() {
        // Arrange
        val settingsRepository = InMemoryShopSettingsRepository(ShopSettings.byDefault())
        val useCase = SendOrderConfirmationEmailUseCase(settingsRepository, userRepository, emailRepository)
        // Act
        useCase.unfold(order)
        // Assert
        assertThat(emailRepository.sentEmails()).containsExactly(OrderConfirmationEmail(Email("jane@example.com"), FullName(FirstName("Jane"), LastName("Doe")), order))
    }
}
