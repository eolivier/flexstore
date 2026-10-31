package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.usecase.ReadCartUseCase
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.InstantProvider

// The use cases that place an order, in the order of the checkout
data class OrderPlacementUseCases(
    val keepOrderAddressesUseCase: KeepOrderAddressesUseCase,
    val placeOrderUseCase: PlaceOrderUseCase,
    val reserveOrderStockUseCase: ReserveOrderStockUseCase
)

// What the checkout reads: the cart of the customer, the settings of the shop, and the order placed
data class CheckoutQueryUseCases(
    val readCartUseCase: ReadCartUseCase,
    val readShopSettingsUseCase: ReadShopSettingsUseCase,
    val readCustomerOrderUseCase: ReadCustomerOrderUseCase
)

// Domain service: a customer places an order from their cart. Their addresses are kept, the order is placed from
// the cart and the threshold of free shipping of the shop, then its units are reserved; the order is read back
// with the id chosen for it, all through the use cases. When a step is refused, the caller keeps nothing of the
// order (the steps run in one transaction)
class OrderCheckoutService(
    private val orderPlacementUseCases: OrderPlacementUseCases,
    private val checkoutQueryUseCases: CheckoutQueryUseCases
) {
    fun placeOrder(checkout: Checkout): Order {
        val orderStamp = OrderStamp(OrderId.nextId(), InstantProvider.now())
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        orderPlacementUseCases.keepOrderAddressesUseCase.unfold(orderPlacement)
        val orderToPlace = OrderToPlace(orderPlacement, contentOf(checkout))
        orderPlacementUseCases.placeOrderUseCase.unfold(orderToPlace)
        orderPlacementUseCases.reserveOrderStockUseCase.unfold(orderStamp.orderId)
        val customerOrderLookup = CustomerOrderLookup(checkout.customer.userId, orderStamp.orderId)
        return checkoutQueryUseCases.readCustomerOrderUseCase.unfold(customerOrderLookup)
    }

    // The cart of the customer, and the threshold of free shipping of the shop
    private fun contentOf(checkout: Checkout): OrderContent {
        val cart = checkoutQueryUseCases.readCartUseCase.unfold(checkout.customer)
        val shopSettings = checkoutQueryUseCases.readShopSettingsUseCase.unfold(Unit)
        return OrderContent(cart, shopSettings.freeShippingThreshold)
    }
}
