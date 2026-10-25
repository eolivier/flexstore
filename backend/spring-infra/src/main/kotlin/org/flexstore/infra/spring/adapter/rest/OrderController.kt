package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.user.model.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonOrder
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderAddresses
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

// The orders of the signed-in customer (the token gives the customer)
@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderUseCaseFacade: OrderUseCaseFacade) {

    @Operation(summary = "Place an order from the cart of the signed-in customer, delivered and billed to the given addresses, then empty the cart")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // The order is stored and the cart emptied together, or not at all
    @Transactional
    fun placeOrder(@AuthenticationPrincipal jwt: Jwt, @RequestBody addresses: JsonOrderAddresses): JsonOrder {
        val checkout = Checkout(CartOwner.Customer(customer(jwt)), addresses.toOrderAddresses())
        val placedOrder = orderUseCaseFacade.placeOrder(checkout)
        return JsonOrder(placedOrder)
    }

    @Operation(summary = "Retrieve the orders of the signed-in customer, newest first")
    @GetMapping
    fun getOrders(@AuthenticationPrincipal jwt: Jwt): List<JsonOrder> =
        orderUseCaseFacade.getOrders(customer(jwt)).asList().map { JsonOrder(it) }

    @Operation(summary = "Retrieve an order of the signed-in customer")
    @GetMapping("/{id}")
    fun getOrder(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): JsonOrder =
        JsonOrder(orderUseCaseFacade.getOrder(customer(jwt), OrderId(id)))

    private fun customer(jwt: Jwt) = UserId.ValidUserId(jwt.subject)
}
