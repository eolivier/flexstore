package org.flexstore.domain.invoice.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.discount.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.product.*
import org.flexstore.domain.seller.*
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
    private val invoiceIssue = InvoiceIssue(OrderId("46555c9f-4e92"), InvoiceDate(placedAt))
    private val invoice = Invoice(InvoiceNumber("2026-000042"), invoiceIssue, testSeller.id)

    private fun documentOf(shippingMethod: ShippingMethod): InvoiceDocument {
        val delivery = OrderDelivery(homeOrderAddresses, shippingMethod)
        val orderPlacement = OrderPlacement(OrderStamp(OrderId("46555c9f-4e92"), placedAt), Checkout(customer, delivery, acceptedTermsOfSale))
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
    fun `the invoice should give the seller with its incorporation and its identifiers, and the customer`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.STANDARD)
        // Assert
        assertThat(invoiceDocument.seller().asList().map { it.value })
            .containsExactly(
                "Flexstore SAS",
                "SAS au capital de 10 000 €",
                "1 rue de la Démo, 75001 Paris, France",
                "Tél. : 01 23 45 67 89",
                "Email : contact@flexstore.fr",
                "Site : www.flexstore.fr",
                "SIRET : 123 456 789 00012",
                "RCS Paris 123 456 789",
                "TVA intracommunautaire : FR12345678901"
            )
        assertThat(invoiceDocument.billedTo().asList().map { it.value })
            .containsSequence("Facturé à", "Jane Doe").contains("jane@example.com")
    }

    @Test
    fun `the invoice should list the lines of the order, then its shipping, with their price without VAT`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.EXPRESS)
        // Assert
        assertThat(invoiceDocument.tableHeader())
            .isEqualTo(InvoiceRow("Désignation", "Quantité", "Prix unitaire HT", "Taux TVA", "Montant TVA", "Total TTC"))
        assertThat(invoiceDocument.tableRows()).isEqualTo(
            InvoiceRows(
                InvoiceRow("Desk lamp", "1", "29,17 €", "20 %", "5,83 €", "35,00 €"),
                InvoiceRow("Livraison express", "1", "8,25 €", "20 %", "1,65 €", "9,90 €")
            )
        )
    }

    @Test
    fun `the discount of the order should be taken off the items on the invoice`() {
        // Arrange
        val validity = DiscountValidity(ExpiryDate(placedAt.plusSeconds(3600)), UsageLimit(5))
        val fiveEurosOff = DiscountCode(PromoCode("FIVE"), DiscountOffer(AmountOff(Price(Amount(BigDecimal(5)), Currency.EUR)), validity))
        val delivery = OrderDelivery(homeOrderAddresses, ShippingMethod.EXPRESS)
        val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), placedAt)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, delivery, acceptedTermsOfSale))
        val terms = OrderTerms(FreeShippingThreshold.byDefault(), WithDiscountCode(fiveEurosOff))
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        val order = Order.place(orderPlacement, OrderContent(cart, terms))
        val invoicedSale = InvoicedSale(AdministeredOrder(order, janeDoe), testSeller)
        // Act
        val invoiceDocument = InvoiceDocument(invoice, invoicedSale)
        // Assert
        assertThat(invoiceDocument.tableRows().asList())
            .contains(InvoiceRow("Remise FIVE", "1", "-4,17 €", "20 %", "-0,83 €", "-5,00 €"))
        assertThat(invoiceDocument.totals().asList().last()).isEqualTo(InvoiceRow("Total TTC", "39,90 €"))
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

    @Test
    fun `the totals should give the VAT of each rate, from the highest`() {
        // Arrange
        val bookPricing = ProductPricing(Price(Amount(BigDecimal(20)), Currency.EUR), VatRate.REDUCED)
        val bookText = ProductText(Name("Cook book"), Description("Recipes"))
        val translations = ProductTranslations.inDefaultLanguage(bookText)
        val bookId = ProductId(Identity("cook-book"))
        val cookBook = DefinedProduct(bookId, Sku.from("BOOK"), translations, Category.BOOKS, bookPricing)
        val delivery = OrderDelivery(homeOrderAddresses, ShippingMethod.EXPRESS)
        val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), placedAt)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, delivery, acceptedTermsOfSale))
        val cart = Cart(customer).add(deskLamp, Quantity(1)).add(cookBook, Quantity(1))
        val order = Order.place(orderPlacement, cart)
        val invoicedSale = InvoicedSale(AdministeredOrder(order, janeDoe), testSeller)
        // Act
        val invoiceDocument = InvoiceDocument(invoice, invoicedSale)
        // Assert
        assertThat(invoiceDocument.totals()).isEqualTo(
            InvoiceRows(
                InvoiceRow("Total HT", "56,79 €"),
                InvoiceRow("TVA 20 %", "6,88 €"),
                InvoiceRow("TVA 5,5 %", "1,23 €"),
                InvoiceRow("Total TTC", "64,90 €")
            )
        )
    }

    @Test
    fun `each row should give its rate and its VAT, the shipping split among the rates of the items`() {
        // Arrange
        val bookPricing = ProductPricing(Price(Amount(BigDecimal(20)), Currency.EUR), VatRate.REDUCED)
        val bookText = ProductText(Name("Cook book"), Description("Recipes"))
        val translations = ProductTranslations.inDefaultLanguage(bookText)
        val bookId = ProductId(Identity("cook-book"))
        val cookBook = DefinedProduct(bookId, Sku.from("BOOK"), translations, Category.BOOKS, bookPricing)
        val delivery = OrderDelivery(homeOrderAddresses, ShippingMethod.EXPRESS)
        val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), placedAt)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, delivery, acceptedTermsOfSale))
        val cart = Cart(customer).add(deskLamp, Quantity(1)).add(cookBook, Quantity(1))
        val order = Order.place(orderPlacement, cart)
        val invoicedSale = InvoicedSale(AdministeredOrder(order, janeDoe), testSeller)
        // Act
        val invoiceDocument = InvoiceDocument(invoice, invoicedSale)
        // Assert
        assertThat(invoiceDocument.tableRows()).isEqualTo(
            InvoiceRows(
                InvoiceRow("Desk lamp", "1", "29,17 €", "20 %", "5,83 €", "35,00 €"),
                InvoiceRow("Cook book", "1", "18,96 €", "5,5 %", "1,04 €", "20,00 €"),
                InvoiceRow("Livraison express", "1", "8,66 €", "20 % / 5,5 %", "1,24 €", "9,90 €")
            )
        )
    }

    @Test
    fun `an invoice of a seller written before its reach and its incorporation were asked should show none`() {
        // Arrange
        val olderSellerDetail =
            testSellerDetail.copy(reach = SellerReach.none(), incorporation = SellerIncorporation.none())
        val olderSeller = testSeller.copy(detail = olderSellerDetail)
        val order = documentOf(ShippingMethod.STANDARD).sale.administeredOrder
        // Act
        val invoiceDocument = InvoiceDocument(invoice, InvoicedSale(order, olderSeller))
        // Assert
        assertThat(invoiceDocument.seller().asList().map { it.value }).containsExactly(
            "Flexstore SAS",
            "1 rue de la Démo, 75001 Paris, France",
            "SIRET : 123 456 789 00012",
            "TVA intracommunautaire : FR12345678901"
        )
    }

    @Test
    fun `an invoice to a private individual should say when it was paid`() {
        // Act
        val invoiceDocument = documentOf(ShippingMethod.STANDARD)
        // Assert
        assertThat(invoiceDocument.footer()).isEqualTo(
            InvoiceLines(
                "Facture acquittée le 24 octobre 2026 : la commande a été payée en ligne.",
                "Prix en euros, TVA française comprise."
            )
        )
    }

    @Test
    fun `an invoice to a company should also give the terms of a late payment`() {
        // Arrange
        val acme = LegalEntity(Company(CompanyName("Acme"), VatNumber("FR12345678901")))
        val acmeAddresses = OrderAddresses.sameFor(PostalAddress(acme, homeLocation))
        val delivery = OrderDelivery(acmeAddresses, ShippingMethod.STANDARD)
        val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), placedAt)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, delivery, acceptedTermsOfSale))
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        val invoicedSale = InvoicedSale(AdministeredOrder(order, janeDoe), testSeller)
        // Act
        val invoiceDocument = InvoiceDocument(invoice, invoicedSale)
        // Assert
        assertThat(invoiceDocument.footer().asList().map { it.value }).endsWith(
            "Pas d'escompte pour paiement anticipé.",
            "En cas de retard de paiement : pénalités au taux d'intérêt légal multiplié par trois, et indemnité forfaitaire de 40 € pour frais de recouvrement."
        )
    }
}
