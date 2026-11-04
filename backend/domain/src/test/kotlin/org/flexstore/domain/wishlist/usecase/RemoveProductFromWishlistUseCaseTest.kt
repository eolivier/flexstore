package org.flexstore.domain.wishlist.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.*
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
import org.flexstore.domain.wishlist.WishlistEntry
import org.flexstore.domain.wishlist.WishlistItem
import org.flexstore.domain.wishlist.WishlistItemId
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryWishlistRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal

class RemoveProductFromWishlistUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val customer = UserId.ValidUserId("user-1")
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val wishlistRepository = InMemoryWishlistRepository(productRepository)
    private val userRepository = customersWithIds(customer.value)
    private val useCase = RemoveProductFromWishlistUseCase(wishlistRepository, userRepository)

    private fun saveToWishlist() {
        val wishlistItem = WishlistItem(WishlistItemId.nextId(), customer, deskLamp.productId, SavedAt.now())
        wishlistRepository.save(wishlistItem)
    }

    @Test
    fun `should remove the product from the wishlist of the customer`() {
        // Arrange
        saveToWishlist()
        // Act
        useCase.unfold(WishlistEntry(customer, deskLamp.productId))
        // Assert
        assertThat(wishlistRepository.contains(customer, deskLamp.productId)).isFalse()
    }

    @Test
    fun `removing a product not saved should leave the wishlist as it is`() {
        // Act
        useCase.unfold(WishlistEntry(customer, deskLamp.productId))
        // Assert
        assertThat(wishlistRepository.findAllBy(customer).asList()).isEmpty()
    }

    @Test
    fun `an unknown customer should not remove a product from a wishlist`() {
        // Arrange
        val entry = WishlistEntry(unknownCustomerId, deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(entry) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not remove a product from their wishlist`(
        status: AccountStatus
    ) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val entry = WishlistEntry(blockedCustomerId, deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(entry) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountRefusedException::class.java)
    }
}
