package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.user.model.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonOrder
import org.flexstore.domain.discount.PromoCode
import org.flexstore.infra.spring.adapter.rest.json.JsonDiscountPreview
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderAddresses
import org.flexstore.infra.spring.adapter.rest.json.JsonShippingOptions
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

// The orders of the signed-in customer (the token gives the customer)
@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderUseCases: OrderUseCases,
    private val orderCheckoutService: OrderCheckoutService
) {

    @Operation(
        summary = "Place an order from the cart of the signed-in customer, delivered and billed to the given " +
            "addresses, and reserve its units until it is paid"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // The order is stored and its units reserved together, or not at all. The business exceptions (an order
    // refused because another one took the units meanwhile) are checked exceptions: Spring would commit on them
    // by default
    @Transactional(rollbackFor = [Exception::class])
    fun placeOrder(@AuthenticationPrincipal jwt: Jwt, @RequestBody addresses: JsonOrderAddresses): JsonOrder {
        val checkout = Checkout(CartOwner.Customer(customer(jwt)), addresses.toOrderDelivery())
        val placedOrder = orderCheckoutService.placeOrder(checkout, addresses.toEnteredDiscountCode())
        return JsonOrder(placedOrder, orderUseCases.paymentDelay())
    }

    @Operation(summary = "Retrieve the shipping methods, with their cost for the cart of the signed-in customer")
    @GetMapping("/shipping-options")
    fun getShippingOptions(@AuthenticationPrincipal jwt: Jwt): JsonShippingOptions {
        val shippingOptions = orderUseCases.shippingOptionsFor(CartOwner.Customer(customer(jwt)))
        return JsonShippingOptions(shippingOptions)
    }

    @Operation(summary = "Retrieve what a discount code takes off the cart of the signed-in customer")
    @GetMapping("/discount-preview")
    fun previewDiscount(@AuthenticationPrincipal jwt: Jwt, @RequestParam code: String): JsonDiscountPreview {
        val customer = CartOwner.Customer(customer(jwt))
        val discountPreview = orderCheckoutService.previewDiscount(customer, PromoCode.from(code))
        return JsonDiscountPreview(discountPreview)
    }

    @Operation(summary = "Retrieve the orders of the signed-in customer, newest first")
    @GetMapping
    fun getOrders(@AuthenticationPrincipal jwt: Jwt): List<JsonOrder> {
        val paymentDelay = orderUseCases.paymentDelay()
        return orderUseCases.getOrders(customer(jwt)).asList().map { JsonOrder(it, paymentDelay) }
    }

    @Operation(summary = "Retrieve an order of the signed-in customer")
    @GetMapping("/{id}")
    fun getOrder(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): JsonOrder {
        val order = orderUseCases.getOrder(customer(jwt), OrderId(id))
        return JsonOrder(order, orderUseCases.paymentDelay())
    }

    private fun customer(jwt: Jwt) = UserId.ValidUserId(jwt.subject)
}
