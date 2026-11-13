package org.flexstore.domain.cart.usecase

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.*
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.actorOf
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.unknownCustomerId
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryCartRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ChangeCartItemQuantityUseCaseTest : WithAssertions {

    private val product = DefinedProduct(
        ProductId("product-1"), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR)
    )
    private val owner = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val userRepository = customersWithIds("user-1", "user-2")
    private val actorResolutionService = ActorResolutionService(userRepository)

    private fun changeQuantityRequest(owner: CartOwner, itemId: ItemId, quantity: Quantity) =
        ChangeCartItemQuantityRequest(actorResolutionService.actorOf(owner), owner, itemId, quantity)
    private val useCase = ChangeCartItemQuantityUseCase(cartRepository)
    private val savedCart = cartRepository.save(Cart(owner).add(product, Quantity(1)))
    private val itemId = savedCart.items.cartItems().itemIds().single()

    @Test
    fun `should change the quantity of the item`() {
        // Arrange
        val request = changeQuantityRequest(owner, itemId, Quantity(4))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load(owner).findBy(itemId)?.quantity).isEqualTo(Quantity(4))
    }

    @Test
    fun `a quantity of zero should remove the item`() {
        // Arrange
        val request = changeQuantityRequest(owner, itemId, Quantity(0))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load(owner).items.asList()).isEmpty()
    }

    @Test
    fun `an item already removed should be ignored`() {
        // Arrange
        useCase.unfold(changeQuantityRequest(owner, itemId, Quantity(0)))
        // Act
        useCase.unfold(changeQuantityRequest(owner, itemId, Quantity(5)))
        // Assert
        assertThat(cartRepository.load(owner).items.asList()).isEmpty()
    }

    @Test
    fun `should not change an item of another cart`() {
        // Arrange
        val otherOwner = CartOwner.Customer(UserId.ValidUserId("user-2"))
        // Act
        useCase.unfold(changeQuantityRequest(otherOwner, itemId, Quantity(0)))
        // Assert
        assertThat(cartRepository.load(owner).findBy(itemId)?.quantity).isEqualTo(Quantity(1))
    }

    @Test
    fun `an unknown customer should not change the quantity of an item`() {
        // Arrange
        val request = changeQuantityRequest(CartOwner.Customer(unknownCustomerId), itemId, Quantity(3))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not change the quantity of an item`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val request = changeQuantityRequest(CartOwner.Customer(blockedCustomerId), itemId, Quantity(3))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
