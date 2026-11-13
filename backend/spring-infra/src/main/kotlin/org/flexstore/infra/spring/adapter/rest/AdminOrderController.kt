package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.invoice.OrderRefundService
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonAdminOrder
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderRefund
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderStatusAdvance
import org.flexstore.infra.spring.adapter.rest.json.JsonOrderTrackingCorrection
import org.springframework.transaction.annotation.Transactional
import org.flexstore.domain.sharedkernel.PageNumber
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The orders of all the customers, with the customer of each one, for the administrators (ADMIN role, see
// SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val orderAdministrationService: OrderAdministrationService,
    private val orderRefundService: OrderRefundService,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(
        summary = "Retrieve a page of the orders of all the customers, newest first, with their customer",
        description = "The total number of orders is in the header X-Total-Count; a size above 100 is " +
            "brought back to 100"
    )
    @GetMapping
    fun getAllOrders(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int
    ): ResponseEntity<List<JsonAdminOrder>> {
        val pageRequest = PageRequest.of(page, size)
        val orders = orderAdministrationService.getAllOrders(actorOf(reader), pageRequest)
        return pagedBody(orders.content.asList().map { JsonAdminOrder(it) }, orders.total)
    }

    @Operation(summary = "Retrieve any order, with its customer")
    @GetMapping("/{orderId}")
    fun getOrder(@AuthenticationPrincipal reader: Jwt, @PathVariable orderId: String): JsonAdminOrder =
        JsonAdminOrder(orderAdministrationService.getOrder(actorOf(reader), OrderId(orderId)))

    @Operation(
        summary = "Move an order to the next status of its life cycle: PROCESSING, SHIPPED, then DELIVERED; " +
            "moving it to SHIPPED may give the carrier and the tracking number of its parcel"
    )
    @PostMapping("/{orderId}/status/advance")
    fun advanceStatus(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody statusAdvance: JsonOrderStatusAdvance
    ): JsonAdminOrder {
        val orderStatusAdvance = statusAdvance.toOrderStatusAdvance(actorOf(editor), OrderId(orderId))
        val advancedOrder = orderAdministrationService.advance(orderStatusAdvance)
        return JsonAdminOrder(advancedOrder)
    }

    @Operation(summary = "Correct the carrier and the tracking number of an order that is shipped or delivered")
    @PutMapping("/{orderId}/tracking")
    fun correctTracking(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody trackingCorrection: JsonOrderTrackingCorrection
    ): JsonAdminOrder {
        val correction = trackingCorrection.toOrderTrackingCorrection(actorOf(editor), OrderId(orderId))
        val correctedOrder = orderAdministrationService.correctTracking(correction)
        return JsonAdminOrder(correctedOrder)
    }

    @Operation(
        summary = "Cancel a paid order before it is shipped: refund its payment, put its units back in stock, " +
            "and cancel its invoice by a credit note"
    )
    @PostMapping("/{orderId}/refund")
    // The refund, the status, the stock and the credit note are stored together, or not at all (the business
    // exceptions are checked exceptions, on which Spring would commit by default)
    @Transactional(rollbackFor = [Exception::class])
    fun refund(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody orderRefund: JsonOrderRefund
    ): JsonAdminOrder {
        val refund = orderRefund.toOrderRefund(actorOf(editor), OrderId(orderId))
        val refundedOrder = orderRefundService.refund(refund)
        return JsonAdminOrder(refundedOrder)
    }

    private fun actorOf(jwt: Jwt) = actorResolutionService.actorOf(UserId.of(jwt.subject))
}
