package org.flexstore.domain.invoice

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class InvoiceDocumentTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val janeDoe = OrderCustomer(FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"))
    private val invoice = Invoice(InvoiceNumber("2026-000042"), OrderId("46555c9f-4e92"), InvoiceDate(placedAt))

    private fun documentOf(shippingMethod: ShippingMethod): InvoiceDocument {
        val delivery = OrderDelivery(homeOrderAddresses, shippingMethod)
        val orderPlacement = OrderPlacement(OrderStamp(OrderId("46555c9f-4e92"), placedAt), Checkout(customer, delivery))
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        return InvoiceDocument(invoice, InvoicedSale(AdministeredOrder(order, janeDoe), testSeller))
    }

    @Test
    fun `the invoice should give its number, its date and the order it invoices`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.STANDARD)
        // Assert
        assertThat(invoiceDocument.title()).isEqualTo(InvoiceText("Facture n° 2026-000042"))
        assertThat(invoiceDocument.dates()).isEqualTo(
            InvoiceLines("Date de facturation : 24 octobre 2026", "Commande n° 46555C9F du 24 octobre 2026")
        )
    }

    @Test
    fun `the invoice should give the seller with its identifiers, and the customer with their email`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.STANDARD)
        // Assert
        assertThat(invoiceDocument.seller().asList().map { it.value })
            .contains("Flexstore SAS", "SIRET : 123 456 789 00012", "TVA intracommunautaire : FR12345678901")
        assertThat(invoiceDocument.billedTo().asList().map { it.value })
            .containsSequence("Facturé à", "Jane Doe").contains("jane@example.com")
    }

    @Test
    fun `the invoice should list the lines of the order, then its shipping`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.EXPRESS)
        // Assert
        assertThat(invoiceDocument.tableRows()).isEqualTo(
            InvoiceRows(
                InvoiceRow("Desk lamp", "1", "35,00 €", "35,00 €"),
                InvoiceRow("Livraison express", "1", "9,90 €", "9,90 €")
            )
        )
    }

    @Test
    fun `the totals should give the price without VAT, the VAT and the price with VAT`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.EXPRESS)
        // Assert
        assertThat(invoiceDocument.totals()).isEqualTo(
            InvoiceRows(
                InvoiceRow("Total HT", "37,42 €"),
                InvoiceRow("TVA 20 %", "7,48 €"),
                InvoiceRow("Total TTC", "44,90 €")
            )
        )
    }
}
