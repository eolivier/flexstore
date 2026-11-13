package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.service.*
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
import org.flexstore.domain.sharedkernel.PageNumber
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// The orders of the signed-in customer (the token gives the customer)
@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderUseCases: OrderUseCases,
    private val orderCheckoutService: OrderCheckoutService,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(
        summary = "Place an order from the cart of the signed-in customer, delivered and billed to the given " +
            "addresses, under the current terms of sale they accepted, and reserve its units until it is paid"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // The order is stored and its units reserved together, or not at all. The business exceptions (an order
    // refused because another one took the units meanwhile) are checked exceptions: Spring would commit on them
    // by default
    @Transactional(rollbackFor = [Exception::class])
    fun placeOrder(@AuthenticationPrincipal jwt: Jwt, @RequestBody addresses: JsonOrderAddresses): JsonOrder {
        val customer = CartOwner.Customer(customer(jwt))
        val checkout = Checkout(customer, addresses.toOrderDelivery(), addresses.toTermsOfSaleAcceptance())
        val discountCode = addresses.toEnteredDiscountCode()
        val placedOrder = orderCheckoutService.placeOrder(actorOf(jwt), checkout, discountCode)
        return JsonOrder(placedOrder, orderUseCases.paymentDelay(actorOf(jwt)))
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
        val discountPreview = orderCheckoutService.previewDiscount(actorOf(jwt), PromoCode.from(code))
        return JsonDiscountPreview(discountPreview)
    }

    @Operation(
        summary = "Retrieve a page of the orders of the signed-in customer, newest first",
        description = "The total number of orders is in the header X-Total-Count; a size above 100 is " +
            "brought back to 100"
    )
    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int
    ): ResponseEntity<List<JsonOrder>> {
        val paymentDelay = orderUseCases.paymentDelay(actorOf(jwt))
        val orders = orderUseCases.getOrders(actorOf(jwt), PageRequest.of(page, size))
        return pagedBody(orders.content.asList().map { JsonOrder(it, paymentDelay) }, orders.total)
    }

    @Operation(summary = "Retrieve an order of the signed-in customer")
    @GetMapping("/{id}")
    fun getOrder(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): JsonOrder {
        val order = orderUseCases.getOrder(customer(jwt), OrderId(id))
        return JsonOrder(order, orderUseCases.paymentDelay(actorOf(jwt)))
    }

    private fun customer(jwt: Jwt) = UserId.ValidUserId(jwt.subject)

    private fun actorOf(jwt: Jwt) = actorResolutionService.actorOf(customer(jwt))
}
