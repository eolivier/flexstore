package org.flexstore.domain.order.usecase

import org.flexstore.infra.repository.InMemoryStockRepository
import org.flexstore.domain.stock.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.ReadCartUseCase
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.flexstore.infra.repository.InMemoryShopSettingsRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OrderCheckoutServiceTest : WithAssertions {

    private val deskLampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), deskLampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val janeEmail = Email("jane@example.com")
    private val definedUser = User.DefinedUser(customer.userId, janeDoe, janeEmail, PlainPassword("password"))
    private val cartRepository = InMemoryCartRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = InMemoryUserRepository().apply { save(definedUser) }
    private val deskLampStock = Stock(deskLamp.productId, StockQuantity(10))
    private val stockRepository = InMemoryStockRepository().apply { save(deskLampStock) }
    private val shopSettingsRepository = InMemoryShopSettingsRepository()
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val checkout = Checkout(customer, homeOrderAddresses)
    private val service = checkoutServiceWith(stockRepository)

    private fun checkoutServiceWith(stockRepository: StockRepository): OrderCheckoutService {
        val orderPlacementUseCases = OrderPlacementUseCases(
            KeepOrderAddressesUseCase(userRepository),
            PlaceOrderUseCase(orderRepository, stockRepository),
            ReserveOrderStockUseCase(orderRepository, stockRepository)
        )
        val checkoutQueryUseCases = CheckoutQueryUseCases(
            ReadCartUseCase(cartRepository),
            ReadShopSettingsUseCase(shopSettingsRepository),
            ReadCustomerOrderUseCase(orderRepository)
        )
        val clock = Clock.fixed(placedAt, ZoneOffset.UTC)
        return OrderCheckoutService(orderPlacementUseCases, checkoutQueryUseCases, clock)
    }

    private fun storedOrders() = orderRepository.findAll().asList()

    private fun postalAddressesOfTheDefinedUser() =
        (userRepository.findById(customer.userId) as User.DefinedUser).postalAddresses.asList()

    @Test
    fun `should store the order placed from the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val order = service.placeOrder(checkout)
        // Assert
        assertThat(storedOrders()).containsExactly(order)
        assertThat(order.lines.asList()).containsExactly(OrderLine.of(deskLamp, Quantity(2)))
        assertThat(order.placedAt).isEqualTo(placedAt)
    }

    @Test
    fun `the order should be shipped as chosen, at the cost of its method`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(1)))
        val expressDelivery = OrderDelivery(homeOrderAddresses, ShippingMethod.EXPRESS)
        // Act
        val order = service.placeOrder(Checkout(customer, expressDelivery))
        // Assert
        val expressCost = Price(Amount(BigDecimal("9.90")), Currency.EUR)
        assertThat(order.shipping).isEqualTo(OrderShipping(ShippingMethod.EXPRESS, expressCost))
        assertThat(order.totalPrice()).isEqualTo(Price(Amount(BigDecimal("44.90")), Currency.EUR))
    }

    @Test
    fun `the standard shipping should be free from the threshold of the shop settings`() {
        // Arrange
        val threshold = FreeShippingThreshold(Price(Amount(BigDecimal(30)), Currency.EUR))
        shopSettingsRepository.save(ShopSettings.byDefault().withFreeShippingThreshold(threshold))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(1)))
        // Act
        val order = service.placeOrder(checkout)
        // Assert
        assertThat(order.shipping.cost).isEqualTo(Price(Amount(BigDecimal.ZERO), Currency.EUR))
    }

    @Test
    fun `the order should reserve its units until it is paid`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        service.placeOrder(checkout)
        // Assert
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(8))
    }

    @Test
    fun `should keep the cart until the order is paid`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        service.placeOrder(checkout)
        // Assert
        assertThat(cartRepository.load(customer).hasItems()).isTrue()
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Act
        val thrown = catchThrowable { service.placeOrder(checkout) }
        // Assert
        assertThat(thrown).isInstanceOf(EmptyCartException::class.java)
        assertThat(storedOrders()).isEmpty()
    }

    @Test
    fun `an unknown customer should not place an order`() {
        // Arrange
        val unknownCustomer = CartOwner.Customer(UserId.ValidUserId("unknown-customer"))
        cartRepository.save(Cart(unknownCustomer).add(deskLamp, Quantity(2)))
        val unknownCustomerCheckout = Checkout(unknownCustomer, homeOrderAddresses)
        // Act
        val thrown = catchThrowable { service.placeOrder(unknownCustomerCheckout) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `the addresses entered when ordering should become addresses of the customer`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        val billedToTheOffice = Checkout(customer, OrderAddresses(homeAddress, officeAddress))
        // Act
        service.placeOrder(billedToTheOffice)
        // Assert
        assertThat(postalAddressesOfTheDefinedUser()).containsExactly(homeAddress, officeAddress)
    }

    @Test
    fun `an address the customer already has should not be added twice`() {
        // Arrange
        userRepository.save(definedUser.copy(postalAddresses = PostalAddresses(homeAddress)))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        service.placeOrder(checkout)
        // Assert
        assertThat(postalAddressesOfTheDefinedUser()).containsExactly(homeAddress)
    }

    @ParameterizedTest(name = "an account {0}: order placed = {1}")
    @CsvSource(
        "PENDING_VERIFICATION, true",
        "ACTIVE, true",
        "SUSPENDED, false",
        "LOCKED, false",
        "DISABLED, false"
    )
    fun `only an account that allows ordering should place an order`(status: AccountStatus, placed: Boolean) {
        // Arrange
        userRepository.save(definedUser.copy(status = status))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val thrown = catchThrowable { service.placeOrder(checkout) }
        // Assert
        assertThat(storedOrders().isEmpty()).isEqualTo(!placed)
        if (!placed) assertThat(thrown).isInstanceOf(OrderRefusedException::class.java)
    }

    @Test
    fun `an order should not be placed when the stock does not cover the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(11)))
        // Act
        val thrown = catchThrowable { service.placeOrder(checkout) }
        // Assert
        assertThat(thrown).isInstanceOf(OutOfStockException::class.java).hasMessageContaining("Desk lamp")
        assertThat(storedOrders()).isEmpty()
    }

    @Test
    fun `an order should be refused when another order took the units after the stock was checked`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        val serviceWithStockTakenMeanwhile = checkoutServiceWith(StockTakenMeanwhile(stockRepository))
        // Act
        val thrown = catchThrowable { serviceWithStockTakenMeanwhile.placeOrder(checkout) }
        // Assert
        assertThat(thrown).isInstanceOf(OutOfStockException::class.java).hasMessageContaining("Desk lamp")
    }
}

// The stock covers the cart when it is checked, but another order takes the units before they are reserved
private class StockTakenMeanwhile(stockRepository: StockRepository) : StockRepository by stockRepository {

    override fun reserve(productId: ProductId, quantity: Quantity) = StockReservation.NOT_COVERED
}
