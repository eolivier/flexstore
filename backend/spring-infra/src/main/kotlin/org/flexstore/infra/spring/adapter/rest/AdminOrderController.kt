package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.invoice.OrderRefundService
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The orders of all the customers, with the customer of each one, for the administrators (ADMIN role, see
// SecurityConfig)
@RestController
@RequestMapping("/api/admin/orders")
class AdminOrderController(
    private val orderAdministrationService: OrderAdministrationService,
    private val orderRefundService: OrderRefundService,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getAllOrders(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int
    ): ResponseEntity<List<JsonAdminOrder>> {
        val pageRequest = PageRequest.of(page, size)
        val actor = actorResolutionService.actorOf(reader)
        val orders = orderAdministrationService.getAllOrders(actor, pageRequest)
        return pagedBody(orders.content.asList().map { JsonAdminOrder(it) }, orders.total)
    }

    @GetMapping("/{orderId}")
    fun getOrder(@AuthenticationPrincipal reader: Jwt, @PathVariable orderId: String): JsonAdminOrder {
        val actor = actorResolutionService.actorOf(reader)
        return JsonAdminOrder(orderAdministrationService.getOrder(actor, OrderId(Identity(orderId))))
    }

    @PostMapping("/{orderId}/status/advance")
    fun advanceStatus(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody statusAdvance: JsonOrderStatusAdvance
    ): JsonAdminOrder {
        val actor = actorResolutionService.actorOf(editor)
        val orderStatusAdvance = statusAdvance.toOrderStatusAdvance(actor, OrderId(Identity(orderId)))
        val advancedOrder = orderAdministrationService.advance(orderStatusAdvance)
        return JsonAdminOrder(advancedOrder)
    }

    @PutMapping("/{orderId}/tracking")
    fun correctTracking(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody trackingCorrection: JsonOrderTrackingCorrection
    ): JsonAdminOrder {
        val actor = actorResolutionService.actorOf(editor)
        val correction = trackingCorrection.toOrderTrackingCorrection(actor, OrderId(Identity(orderId)))
        val correctedOrder = orderAdministrationService.correctTracking(correction)
        return JsonAdminOrder(correctedOrder)
    }

    @PostMapping("/{orderId}/refund")
    // The refund, the status, the stock and the credit note are stored together, or not at all (the business
    // exceptions are checked exceptions, on which Spring would commit by default)
    @Transactional(rollbackFor = [Exception::class])
    fun refund(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable orderId: String,
        @RequestBody orderRefund: JsonOrderRefund
    ): JsonAdminOrder {
        val refund = orderRefund.toOrderRefund(actorResolutionService.actorOf(editor), OrderId(Identity(orderId)))
        val refundedOrder = orderRefundService.refund(refund)
        return JsonAdminOrder(refundedOrder)
    }
}
