package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonAdminOrder
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderRefund
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderStatusAdvance
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

// The orders of all the customers, with the customer of each one, for the administrators (ADMIN role, see
// SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(private val orderAdministrationService: OrderAdministrationService) {

    @Operation(summary = "Retrieve the orders of all the customers, newest first, with their customer")
    @GetMapping
    fun getAllOrders(): List<JsonAdminOrder> =
        orderAdministrationService.getAllOrders().asList().map { JsonAdminOrder(it) }

    @Operation(summary = "Retrieve any order, with its customer")
    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: String): JsonAdminOrder =
        JsonAdminOrder(orderAdministrationService.getOrder(OrderId(orderId)))

    @Operation(summary = "Move an order to the next status of its life cycle: PROCESSING, SHIPPED, then DELIVERED")
    @PostMapping("/{orderId}/status/advance")
    fun advanceStatus(
        @PathVariable orderId: String,
        @RequestBody statusAdvance: JsonOrderStatusAdvance
    ): JsonAdminOrder {
        val orderStatusAdvance = statusAdvance.toOrderStatusAdvance(OrderId(orderId))
        val advancedOrder = orderAdministrationService.advance(orderStatusAdvance)
        return JsonAdminOrder(advancedOrder)
    }

    @Operation(summary = "Cancel a paid order before it is shipped: refund its payment, put its units back in stock")
    @PostMapping("/{orderId}/refund")
    // The refund, the status and the stock are stored together, or not at all (the business exceptions are
    // checked exceptions, on which Spring would commit by default)
    @Transactional(rollbackFor = [Exception::class])
    fun refund(@PathVariable orderId: String, @RequestBody orderRefund: JsonOrderRefund): JsonAdminOrder {
        val refundedOrder = orderAdministrationService.refund(orderRefund.toOrderRefund(OrderId(orderId)))
        return JsonAdminOrder(refundedOrder)
    }
}
