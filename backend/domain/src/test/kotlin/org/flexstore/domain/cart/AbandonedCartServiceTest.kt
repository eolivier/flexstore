package org.flexstore.domain.cart

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

class AbandonedCartServiceTest : WithAssertions {

    private val leftAt = Instant.parse("2026-11-05T10:00:00Z")
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val oneDay = AbandonedCartDelay(24)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val visitor = CartOwner.Visitor(VisitorCartId.nextId())
    private val cartRepository = InMemoryCartRepository()
    private val emailRepository = InMemoryEmailRepository()
    private val settingsRepository = InMemoryShopSettingsRepository(
        ShopSettings.byDefault().withAbandonedCartReminderEnabled().withAbandonedCartDelay(oneDay)
    )
    private val service = AbandonedCartService(
        ReadShopSettingsUseCase(settingsRepository),
        ListAbandonedCartsUseCase(cartRepository),
        RemindAbandonedCartUseCase(cartRepository, customersWithIds("user-1"), emailRepository)
    )

    @BeforeEach
    fun leaveTheCart() {
        InstantProvider.fixAt(leftAt)
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(1)))
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun hoursLater(hours: Long) = InstantProvider.fixAt(leftAt + Duration.ofHours(hours))

    @Test
    fun `should remind the customer of the cart left for the delay`() {
        // Arrange
        hoursLater(24)
        // Act
        val remindedCarts = service.remindAbandonedCarts()
        // Assert
        assertThat(remindedCarts.asList()).extracting("owner").containsExactly(customer)
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).hasSize(1)
    }

    @Test
    fun `should not remind the customer before the delay`() {
        // Arrange
        hoursLater(23)
        // Act
        val remindedCarts = service.remindAbandonedCarts()
        // Assert
        assertThat(remindedCarts.isEmpty()).isTrue()
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @Test
    fun `should not remind the customer when the shop settings turn the reminder off`() {
        // Arrange
        settingsRepository.save(settingsRepository.load().withAbandonedCartReminderDisabled())
        hoursLater(48)
        // Act
        val remindedCarts = service.remindAbandonedCarts()
        // Assert
        assertThat(remindedCarts.isEmpty()).isTrue()
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }

    @Test
    fun `should remind the customer once`() {
        // Arrange
        hoursLater(30)
        service.remindAbandonedCarts()
        hoursLater(60)
        // Act
        val remindedCarts = service.remindAbandonedCarts()
        // Assert
        assertThat(remindedCarts.isEmpty()).isTrue()
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).hasSize(1)
    }

    @Test
    fun `should remind the customer again when the cart changes and is left again`() {
        // Arrange
        hoursLater(30)
        service.remindAbandonedCarts()
        cartRepository.save(cartRepository.load(customer).add(deskLamp, Quantity(2)))
        hoursLater(60)
        // Act
        service.remindAbandonedCarts()
        // Assert
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).hasSize(2)
    }

    @Test
    fun `should use the delay of the shop settings`() {
        // Arrange
        settingsRepository.save(settingsRepository.load().withAbandonedCartDelay(AbandonedCartDelay(2)))
        hoursLater(2)
        // Act
        service.remindAbandonedCarts()
        // Assert
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).hasSize(1)
    }

    @Test
    fun `should not remind a visitor, who has no email`() {
        // Arrange
        cartRepository.delete(customer)
        cartRepository.save(Cart(visitor).add(deskLamp, Quantity(1)))
        hoursLater(48)
        // Act
        val remindedCarts = service.remindAbandonedCarts()
        // Assert
        assertThat(remindedCarts.isEmpty()).isTrue()
        assertThat(emailRepository.sentAbandonedCartReminderEmails()).isEmpty()
    }
}
