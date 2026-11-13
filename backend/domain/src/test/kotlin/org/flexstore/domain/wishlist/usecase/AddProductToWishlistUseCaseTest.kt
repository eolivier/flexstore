package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.actor.ActorResolutionService
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.domain.wishlist.WishlistEntry
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryWishlistRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal

class AddProductToWishlistUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val customer = UserId.ValidUserId("user-1")
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val wishlistRepository = InMemoryWishlistRepository(productRepository)
    private val userRepository = customersWithIds(customer.value, "user-2")
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val useCase = AddProductToWishlistUseCase(wishlistRepository, productRepository)

    @Test
    fun `should save the product to the wishlist of the customer`() {
        // Act
        useCase.unfold(WishlistEntry(actorResolutionService.actorOf(customer), deskLamp.productId))
        // Assert
        assertThat(wishlistRepository.contains(customer, deskLamp.productId)).isTrue()
    }

    @Test
    fun `saving the product again should leave the wishlist as it is`() {
        // Arrange
        useCase.unfold(WishlistEntry(actorResolutionService.actorOf(customer), deskLamp.productId))
        // Act
        useCase.unfold(WishlistEntry(actorResolutionService.actorOf(customer), deskLamp.productId))
        // Assert
        assertThat(wishlistRepository.findAllBy(customer).asList()).hasSize(1)
    }

    @Test
    fun `should not save a product missing from the catalog`() {
        // Arrange
        val entry = WishlistEntry(actorResolutionService.actorOf(customer), ProductId(Identity("unknown")))
        // Act
        val thrown = catchThrowable { useCase.unfold(entry) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductNotFoundException::class.java)
        assertThat(wishlistRepository.findAllBy(customer).asList()).isEmpty()
    }

    @Test
    fun `should save the product to the wishlist of its customer only`() {
        // Arrange
        val otherCustomer = UserId.ValidUserId("user-2")
        // Act
        useCase.unfold(WishlistEntry(actorResolutionService.actorOf(customer), deskLamp.productId))
        // Assert
        assertThat(wishlistRepository.contains(otherCustomer, deskLamp.productId)).isFalse()
    }

    @Test
    fun `an unknown customer should not save a product to a wishlist`() {
        // Arrange
        val entry = WishlistEntry(actorResolutionService.actorOf(unknownCustomerId), deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(entry) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not save a product to their wishlist`(
        status: AccountStatus
    ) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val entry = WishlistEntry(actorResolutionService.actorOf(blockedCustomerId), deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(entry) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
