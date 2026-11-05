package org.flexstore.domain.invoice

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.model.CreditNote
import org.flexstore.domain.invoice.model.CreditNoteDate
import org.flexstore.domain.invoice.model.CreditNoteDocument
import org.flexstore.domain.invoice.model.CreditNoteIssue
import org.flexstore.domain.invoice.model.CreditNoteNumber
import org.flexstore.domain.invoice.model.Invoice
import org.flexstore.domain.invoice.model.InvoiceDate
import org.flexstore.domain.invoice.model.InvoiceDocument
import org.flexstore.domain.invoice.model.InvoiceIssue
import org.flexstore.domain.invoice.model.InvoiceLines
import org.flexstore.domain.invoice.model.InvoiceNumber
import org.flexstore.domain.invoice.model.InvoiceText
import org.flexstore.domain.invoice.model.InvoicedSale
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class CreditNoteDocumentTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderId = OrderId("46555c9f-4e92")
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val janeDoe = OrderCustomer(FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"))
    private val invoice = Invoice(InvoiceNumber("2026-000042"), InvoiceIssue(orderId, InvoiceDate(placedAt)), testSeller.id)
    private val creditNoteIssue = CreditNoteIssue(orderId, CreditNoteDate(Instant.parse("2026-10-26T09:00:00Z")))
    private val creditNote = CreditNote(CreditNoteNumber("AV-2026-000007"), creditNoteIssue)

    private fun invoiceDocument(): InvoiceDocument {
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, placedAt), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        return InvoiceDocument(invoice, InvoicedSale(AdministeredOrder(order, janeDoe), testSeller))
    }

    @Test
    fun `the credit note should give its number, its date and the invoice it cancels`() {
        // Act
        val creditNoteDocument = CreditNoteDocument(creditNote, invoiceDocument())
        // Assert
        assertThat(creditNoteDocument.title()).isEqualTo(InvoiceText("Avoir n° AV-2026-000007"))
        assertThat(creditNoteDocument.dates()).isEqualTo(
            InvoiceLines(
                "Date de l'avoir : 26 octobre 2026",
                "Annule la facture n° 2026-000042 du 24 octobre 2026",
                "Commande n° 46555C9F du 24 octobre 2026"
            )
        )
    }

    @Test
    fun `the credit note should repeat the parties, the rows and the totals of the invoice it cancels`() {
        // Arrange
        val invoiceDocument = invoiceDocument()
        // Act
        val creditNoteDocument = CreditNoteDocument(creditNote, invoiceDocument)
        // Assert
        assertThat(creditNoteDocument.seller()).isEqualTo(invoiceDocument.seller())
        assertThat(creditNoteDocument.billedTo()).isEqualTo(invoiceDocument.billedTo())
        assertThat(creditNoteDocument.tableRows()).isEqualTo(invoiceDocument.tableRows())
        assertThat(creditNoteDocument.totals()).isEqualTo(invoiceDocument.totals())
    }

    @Test
    fun `the credit note should tell that the payment was given back`() {
        // Act
        val creditNoteDocument = CreditNoteDocument(creditNote, invoiceDocument())
        // Assert
        assertThat(creditNoteDocument.footer().asList().first())
            .isEqualTo(InvoiceText("Avoir remboursé : le paiement de la commande a été rendu au client."))
    }
}
