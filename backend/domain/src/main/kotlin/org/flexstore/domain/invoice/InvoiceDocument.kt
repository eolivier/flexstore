package org.flexstore.domain.invoice

import org.flexstore.domain.order.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Price
import org.flexstore.domain.user.model.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FRENCH = Locale.FRANCE
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", FRENCH).withZone(ZoneId.of("Europe/Paris"))

// A text written on an invoice
data class InvoiceText(val value: String)

// Lines of text written one under the other (a block of addresses, the dates...)
data class InvoiceLines(private val lines: List<InvoiceText>) {

    constructor(vararg lines: String) : this(lines.map { InvoiceText(it) })

    // Read-only view for the renderer
    fun asList(): List<InvoiceText> = lines.toList()
}

// A row of a table of the invoice, one text per column
data class InvoiceRow(private val cells: List<InvoiceText>) {

    constructor(vararg cells: String) : this(cells.map { InvoiceText(it) })

    // Read-only view for the renderer
    fun asList(): List<InvoiceText> = cells.toList()
}

// The rows of a table of the invoice
data class InvoiceRows(private val rows: List<InvoiceRow>) {

    constructor(vararg rows: InvoiceRow) : this(rows.toList())

    // Read-only view for the renderer
    fun asList(): List<InvoiceRow> = rows.toList()
}

// What is invoiced: a paid order, and the customer who placed it
data class InvoicedSale(val administeredOrder: AdministeredOrder, val seller: Seller) {

    val order get() = administeredOrder.order
}

// The content of an invoice, as the product owner specifies it (in French, the language of the shop): who sells,
// who buys, what, at which price with and without VAT. The renderer only lays it out
data class InvoiceDocument(val invoice: Invoice, val sale: InvoicedSale) {

    private val order get() = sale.order

    private val vatBreakdown get() = VatBreakdown(order.totalPrice(), VatRate.standard())

    fun title() = InvoiceText("Facture n° ${invoice.number.value}")

    fun dates() = InvoiceLines(
        "Date de facturation : ${DATE_FORMAT.format(invoice.issuedAt.value)}",
        "Commande n° ${order.number()} du ${DATE_FORMAT.format(order.placedAt)}"
    )

    fun seller(): InvoiceLines {
        val (contact, identifiers) = sale.seller.details
        return InvoiceLines(
            contact.name.value,
            contact.address.value,
            "SIRET : ${identifiers.siret.value}",
            "TVA intracommunautaire : ${identifiers.vatNumber.value}"
        )
    }

    // The billing address, and the email of the customer
    fun billedTo(): InvoiceLines {
        val billingAddress = order.addresses.billing
        val email = sale.administeredOrder.customer.email.value
        val vatNumber = vatNumberOf(billingAddress.recipient)
        val lines = listOf("Facturé à") + linesOf(billingAddress) + listOfNotNull(vatNumber) + email
        return InvoiceLines(*lines.toTypedArray())
    }

    fun deliveredTo(): InvoiceLines {
        val lines = listOf("Livré à") + linesOf(order.addresses.delivery)
        return InvoiceLines(*lines.toTypedArray())
    }

    fun tableHeader() = InvoiceRow("Désignation", "Quantité", "Prix unitaire TTC", "Total TTC")

    // The lines of the order, its discount, then its shipping
    fun tableRows(): InvoiceRows {
        val productRows = order.lines.asList().map { line ->
            InvoiceRow(line.productName.value, "${line.quantity.value}", priceOf(line.unitPrice), priceOf(line.linePrice()))
        }
        val shippingCost = priceOf(order.shipping.cost)
        val shippingRow = InvoiceRow(order.shipping.method.label().value, "1", shippingCost, shippingCost)
        return InvoiceRows(productRows + discountRows() + shippingRow)
    }

    // The discount of the order, taken off its items, if any
    private fun discountRows(): List<InvoiceRow> {
        val discount = order.discount as? AppliedOrderDiscount ?: return emptyList()
        val amountOff = "-${priceOf(discount.amount)}"
        return listOf(InvoiceRow(discount.label().value, "1", amountOff, amountOff))
    }

    fun totals() = InvoiceRows(
        InvoiceRow("Total HT", priceOf(vatBreakdown.priceExcludingVat())),
        InvoiceRow("TVA ${vatBreakdown.rate.percent.toPlainString()} %", priceOf(vatBreakdown.vat())),
        InvoiceRow("Total TTC", priceOf(order.totalPrice()))
    )

    fun footer() = InvoiceLines(
        "Facture acquittée : la commande a été payée en ligne.",
        "Prix en euros, TVA française comprise."
    )

    private fun priceOf(price: Price) = price.formattedIn(FRENCH)

    private fun linesOf(address: PostalAddress): List<String> {
        val location = address.location
        val street = listOfNotNull(location.street.value, location.complement?.value).joinToString(", ")
        val town = "${location.postalCode.value} ${location.city.value}"
        return listOf(recipientOf(address.recipient), street, town, location.country.value)
    }

    private fun recipientOf(recipient: Recipient) = when (recipient) {
        is PrivateIndividual -> recipient.fullName.value
        is LegalEntity -> recipient.company.name.value
    }

    private fun vatNumberOf(recipient: Recipient) = when (recipient) {
        is LegalEntity -> recipient.company.vatNumber.takeIf { it.isGiven() }?.let { "TVA : ${it.value}" }
        is PrivateIndividual -> null
    }
}
