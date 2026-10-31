package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.invoice.InvoicePdf
import org.flexstore.domain.invoice.usecase.InvoiceService
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.user.model.UserId
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

// The invoices of the paid orders, as PDF documents: for their customer, or for the administrators (ADMIN role,
// see SecurityConfig)
@Tag(name = "Invoices")
@RestController
class InvoiceController(private val invoiceService: InvoiceService) {

    @Operation(summary = "Download the invoice of a paid order of the signed-in customer (PDF)")
    @GetMapping("/api/orders/{id}/invoice", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getInvoice(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: String): ResponseEntity<ByteArray> {
        val invoicePdf = invoiceService.customerInvoice(UserId.ValidUserId(jwt.subject), OrderId(id))
        return pdfResponse(invoicePdf, id)
    }

    @Operation(summary = "Download the invoice of any paid order (PDF)")
    @GetMapping("/api/admin/orders/{id}/invoice", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getAnyInvoice(@PathVariable id: String): ResponseEntity<ByteArray> =
        pdfResponse(invoiceService.anyInvoice(OrderId(id)), id)

    // Saved by the browser as facture-<number of the order>.pdf
    private fun pdfResponse(invoicePdf: InvoicePdf, orderId: String): ResponseEntity<ByteArray> {
        val fileName = "facture-${orderId.take(ORDER_NUMBER_LENGTH).uppercase()}.pdf"
        val contentDisposition = ContentDisposition.attachment().filename(fileName).build()
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .body(invoicePdf.content)
    }
}

private const val ORDER_NUMBER_LENGTH = 8
