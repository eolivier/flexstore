package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.user.model.*
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
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderUseCases: OrderUseCases,
    private val orderCheckoutService: OrderCheckoutService,
    private val shippingMethodUseCases: ShippingMethodUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // The order is stored and its units reserved together, or not at all. The business exceptions (an order
    // refused because another one took the units meanwhile) are checked exceptions: Spring would commit on them
    // by default
    @Transactional(rollbackFor = [Exception::class])
    fun placeOrder(@AuthenticationPrincipal jwt: Jwt, @RequestBody addresses: JsonOrderAddresses): JsonOrder {
        val customer = CartOwner.Customer(customer(jwt))
        val shippingMethod = shippingMethodUseCases.getEnabled(customer, addresses.toShippingMethodId())
        val delivery = addresses.toOrderDelivery(shippingMethod)
        val checkout = Checkout(customer, delivery, addresses.toTermsOfSaleAcceptance())
        val discountCode = addresses.toEnteredDiscountCode()
        val placedOrder = orderCheckoutService.placeOrder(actorResolutionService.actorOf(jwt), checkout, discountCode)
        return JsonOrder(placedOrder, orderUseCases.paymentDelay(actorResolutionService.actorOf(jwt)))
    }

    @GetMapping("/shipping-options")
    fun getShippingOptions(@AuthenticationPrincipal jwt: Jwt): JsonShippingOptions {
        val shippingOptions = orderUseCases.shippingOptionsFor(CartOwner.Customer(customer(jwt)))
        return JsonShippingOptions(shippingOptions)
    }

    @GetMapping("/discount-preview")
    fun previewDiscount(@AuthenticationPrincipal jwt: Jwt, @RequestParam code: String): JsonDiscountPreview {
        val actor = actorResolutionService.actorOf(jwt)
        val discountPreview = orderCheckoutService.previewDiscount(actor, PromoCode.from(code))
        return JsonDiscountPreview(discountPreview)
    }

    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int
    ): ResponseEntity<List<JsonOrder>> {
        val paymentDelay = orderUseCases.paymentDelay(actorResolutionService.actorOf(jwt))
        val orders = orderUseCases.getOrders(actorResolutionService.actorOf(jwt), PageRequest.of(page, size))
        return pagedBody(orders.content.asList().map { JsonOrder(it, paymentDelay) }, orders.total)
    }

    @GetMapping("/{id}")
    fun getOrder(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): JsonOrder {
        val order = orderUseCases.getOrder(actorResolutionService.actorOf(jwt), OrderId(Identity(id)))
        return JsonOrder(order, orderUseCases.paymentDelay(actorResolutionService.actorOf(jwt)))
    }

    private fun customer(jwt: Jwt) = UserId.ValidUserId(jwt.subject)
}
