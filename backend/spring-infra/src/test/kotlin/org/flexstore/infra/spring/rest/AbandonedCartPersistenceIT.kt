package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.cart.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.cart.JpaCartRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Duration

// The administration account created by the migrations
private const val CUSTOMER_ID = "u-admin"
private const val CATALOG_PRODUCT_ID = "1"

// The carts left by their customer, read and marked reminded in PostgreSQL
@ActiveProfiles("test")
@SpringBootTest
class AbandonedCartPersistenceIT {

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var jpaCartRepository: JpaCartRepository

    @Autowired
    private lateinit var jpaCartItemRepository: JpaCartItemRepository

    private val customer = CartOwner.Customer(UserId.ValidUserId(CUSTOMER_ID))
    private val limitInTheFuture = AbandonmentLimit(InstantProvider.now() + Duration.ofHours(1))
    private val limitInThePast = AbandonmentLimit(InstantProvider.now() - Duration.ofHours(1))

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        jpaCartItemRepository.deleteAll()
        jpaCartRepository.deleteAll()
    }

    private fun leaveCart() {
        val product = productRepository.findById(ProductId(Identity(CATALOG_PRODUCT_ID)))!!
        cartRepository.save(Cart(customer).add(product, Quantity(2)))
    }

    @Test
    fun `a cart left before the limit should be abandoned`() {
        // Arrange
        leaveCart()
        // Act
        val abandonedCarts = cartRepository.abandonedCartsAt(limitInTheFuture)
        // Assert
        assertThat(abandonedCarts.asList()).extracting("owner").containsExactly(customer)
    }

    @Test
    fun `a cart changed after the limit should not be abandoned`() {
        // Arrange
        leaveCart()
        // Act
        val abandonedCarts = cartRepository.abandonedCartsAt(limitInThePast)
        // Assert
        assertThat(abandonedCarts.isEmpty()).isTrue()
    }

    @Test
    fun `a cart of a visitor should not be abandoned`() {
        // Arrange
        val product = productRepository.findById(ProductId(Identity(CATALOG_PRODUCT_ID)))!!
        cartRepository.save(Cart(CartOwner.Visitor(VisitorCartId.nextId())).add(product, Quantity(1)))
        // Act
        val abandonedCarts = cartRepository.abandonedCartsAt(limitInTheFuture)
        // Assert
        assertThat(abandonedCarts.isEmpty()).isTrue()
    }

    @Test
    fun `a cart marked reminded should not be abandoned any more`() {
        // Arrange
        leaveCart()
        // Act
        cartRepository.markReminded(customer)
        // Assert
        assertThat(cartRepository.isReminded(customer)).isTrue()
        assertThat(cartRepository.abandonedCartsAt(limitInTheFuture).isEmpty()).isTrue()
    }

    @Test
    fun `a reminded cart that changes should be abandoned again`() {
        // Arrange
        leaveCart()
        cartRepository.markReminded(customer)
        // Act
        cartRepository.save(cartRepository.load(customer).emptied().add(productInCatalog(), Quantity(3)))
        // Assert
        assertThat(cartRepository.isNotReminded(customer)).isTrue()
        assertThat(cartRepository.abandonedCartsAt(limitInTheFuture).asList()).hasSize(1)
    }

    private fun productInCatalog() = productRepository.findById(ProductId(Identity(CATALOG_PRODUCT_ID)))!!
}
