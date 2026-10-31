package org.flexstore.infra.invoice

import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.parser.PdfTextExtractor
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class OpenPdfInvoiceRendererTest {

    private val sellerContact =
        SellerContact(SellerName("Flexstore SAS"), SellerAddress("1 rue de la Démo, 75001 Paris, France"))
    private val sellerIdentifiers = SellerIdentifiers(Siret("123 456 789 00012"), SellerVatNumber("FR12345678901"))
    private val seller = Seller(SellerId(Identity("seller-1")), SellerDetails(sellerContact, sellerIdentifiers))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")

    private fun invoiceDocument(): InvoiceDocument {
        val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Lampe été"), Price(Amount(BigDecimal(35)), Currency.EUR))
        val home = PostalAddress(
            PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe"))),
            Location(StreetAddress(Street("12 rue des Lilas")), Locality(Town(PostalCode("75011"), City("Paris")), Country("France")))
        )
        val delivery = OrderDelivery(OrderAddresses.sameFor(home), ShippingMethod.EXPRESS)
        val placement = OrderPlacement(OrderStamp(OrderId("46555c9f-4e92"), placedAt), Checkout(customer, delivery))
        val order = Order.place(placement, Cart(customer).add(lamp, Quantity(1)))
        val invoiceIssue = InvoiceIssue(order.orderId, InvoiceDate(placedAt))
        val invoice = Invoice(InvoiceNumber("2026-000042"), invoiceIssue, seller.id)
        val orderCustomer = OrderCustomer(FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"))
        return InvoiceDocument(invoice, InvoicedSale(AdministeredOrder(order, orderCustomer), seller))
    }

    @Test
    fun `the invoice should be a PDF that writes its content, accents and euro sign included`() {
        // Act
        val invoicePdf = OpenPdfInvoiceRenderer().render(invoiceDocument())
        // Assert
        val text = PdfTextExtractor(PdfReader(invoicePdf.content)).getTextFromPage(1)
        assertTrue(String(invoicePdf.content.copyOf(5)) == "%PDF-")
        listOf("Facture n° 2026-000042", "Flexstore SAS", "Lampe été", "Livraison express", "Total TTC", "44,90 €")
            .forEach { expected -> assertTrue(text.contains(expected), "\"$expected\" not in:\n$text") }
    }
}
