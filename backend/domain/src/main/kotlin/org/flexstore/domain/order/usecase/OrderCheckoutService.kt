package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.cart.usecase.ReadCartUseCase
import org.flexstore.domain.discount.*
import org.flexstore.domain.discount.usecase.CheckoutDiscountUseCases
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.InstantProvider
import java.time.Instant

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

// Domain service: a customer places an order from their cart, maybe with a discount code. Their addresses are
// kept; the discount code entered is read usable; the order is placed from the cart, the threshold of free
// shipping of the shop and the discount code, which counts one more use; then its units are reserved. The order
// is read back with the id chosen for it, all through the use cases. When a step is refused, the caller keeps
// nothing of the order (the steps run in one transaction)
class OrderCheckoutService(
    private val orderPlacementUseCases: OrderPlacementUseCases,
    private val checkoutQueryUseCases: CheckoutQueryUseCases,
    private val checkoutDiscountUseCases: CheckoutDiscountUseCases
) {
    // Without discount code
    fun placeOrder(checkout: Checkout) = placeOrder(checkout, NoDiscountCodeEntered)

    fun placeOrder(checkout: Checkout, enteredDiscountCode: EnteredDiscountCode): Order {
        val orderStamp = OrderStamp(OrderId.nextId(), InstantProvider.now())
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        orderPlacementUseCases.keepOrderAddressesUseCase.unfold(orderPlacement)
        val discountChoice = discountChoiceOf(enteredDiscountCode, orderStamp)
        val orderToPlace = OrderToPlace(orderPlacement, contentOf(checkout, discountChoice))
        orderPlacementUseCases.placeOrderUseCase.unfold(orderToPlace)
        if (discountChoice is WithDiscountCode) {
            checkoutDiscountUseCases.useDiscountCodeUseCase.unfold(discountChoice.discountCode.code)
        }
        orderPlacementUseCases.reserveOrderStockUseCase.unfold(orderStamp.orderId)
        val customerOrderLookup = CustomerOrderLookup(checkout.customer.userId, orderStamp.orderId)
        return checkoutQueryUseCases.readCustomerOrderUseCase.unfold(customerOrderLookup)
    }

    // What the discount code entered takes off the cart of the customer, before they order
    fun previewDiscount(customer: CartOwner.Customer, code: PromoCode): DiscountPreview {
        val discountCode = usableDiscountCode(code, InstantProvider.now())
        val itemsPrice = checkoutQueryUseCases.readCartUseCase.unfold(customer).items.totalPrice()
        return DiscountPreview(discountCode.code, discountCode.reduction.amountOff(itemsPrice))
    }

    private fun discountChoiceOf(enteredDiscountCode: EnteredDiscountCode, orderStamp: OrderStamp) =
        when (enteredDiscountCode) {
            NoDiscountCodeEntered -> WithoutDiscountCode
            is DiscountCodeEntered -> {
                val discountCode = usableDiscountCode(enteredDiscountCode.code, orderStamp.placedAt)
                WithDiscountCode(discountCode)
            }
        }

    private fun usableDiscountCode(code: PromoCode, at: Instant): DiscountCode {
        val discountCodeLookup = DiscountCodeLookup(code, at)
        return checkoutDiscountUseCases.readUsableDiscountCodeUseCase.unfold(discountCodeLookup)
    }

    // The cart of the customer, and the terms of the shop: its threshold of free shipping, the discount code
    // chosen
    private fun contentOf(checkout: Checkout, discountChoice: DiscountChoice): OrderContent {
        val cart = checkoutQueryUseCases.readCartUseCase.unfold(checkout.customer)
        val shopSettings = checkoutQueryUseCases.readShopSettingsUseCase.unfold(Unit)
        return OrderContent(cart, OrderTerms(shopSettings.freeShippingThreshold, discountChoice))
    }
}
