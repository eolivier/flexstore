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
import org.flexstore.domain.wishlist.SavedAt
import org.flexstore.domain.wishlist.WishlistItem
import org.flexstore.domain.wishlist.WishlistItemId
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryWishlistRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.Instant

class ListWishlistUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val officeChair = DefinedProduct(
        ProductId(Identity("office-chair")), Name("Office chair"), Price(Amount(BigDecimal(120)), Currency.EUR)
    )
    private val customer = UserId.ValidUserId("user-1")
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp); save(officeChair) }
    private val wishlistRepository = InMemoryWishlistRepository(productRepository)
    private val userRepository = customersWithIds(customer.value)
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val useCase = ListWishlistUseCase(wishlistRepository)

    private fun saveToWishlist(product: DefinedProduct, savedAt: Instant) {
        val wishlistItem = WishlistItem(WishlistItemId.nextId(), customer, product.productId, SavedAt(savedAt))
        wishlistRepository.save(wishlistItem)
    }

    @Test
    fun `should list the products saved by the customer, newest first`() {
        // Arrange
        saveToWishlist(deskLamp, Instant.parse("2026-01-01T10:00:00Z"))
        saveToWishlist(officeChair, Instant.parse("2026-01-02T10:00:00Z"))
        // Act
        val wishlist = useCase.unfold(actorResolutionService.actorOf(customer))
        // Assert
        assertThat(wishlist.asList()).containsExactly(officeChair, deskLamp)
    }

    @Test
    fun `a customer without any saved product should have an empty wishlist`() {
        // Act
        val wishlist = useCase.unfold(actorResolutionService.actorOf(customer))
        // Assert
        assertThat(wishlist.asList()).isEmpty()
    }

    @Test
    fun `an unknown customer should not list their wishlist`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(actorResolutionService.actorOf(unknownCustomerId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not list their wishlist`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        // Act
        val thrown = catchThrowable { useCase.unfold(actorResolutionService.actorOf(blockedCustomerId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
