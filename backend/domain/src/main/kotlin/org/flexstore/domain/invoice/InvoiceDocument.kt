package org.flexstore.domain.invoice

import org.flexstore.domain.order.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Price
import org.flexstore.domain.sharedkernel.VatRate
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

// A document of the invoicing of a sale, an invoice or a credit note, laid out the same way by the renderer: its
// title and dates, who sells, who buys, the table of what is sold, its totals and a footer
interface BillingDocument {
    fun title(): InvoiceText
    fun dates(): InvoiceLines
    fun seller(): InvoiceLines
    fun billedTo(): InvoiceLines
    fun deliveredTo(): InvoiceLines
    fun tableHeader(): InvoiceRow
    fun tableRows(): InvoiceRows
    fun totals(): InvoiceRows
    fun footer(): InvoiceLines
}

// The content of an invoice, as the product owner specifies it (in French, the language of the shop): who sells,
// who buys, what, at which price with and without VAT. The renderer only lays it out
data class InvoiceDocument(val invoice: Invoice, val sale: InvoicedSale) : BillingDocument {

    private val order get() = sale.order

    private val vatBreakdowns get() = VatBreakdowns.of(order)

    override fun title() = InvoiceText("Facture n° ${invoice.number.value}")

    override fun dates() = InvoiceLines(
        "Date de facturation : ${DATE_FORMAT.format(invoice.issuedAt.value)}",
        "Commande n° ${order.number()} du ${DATE_FORMAT.format(order.placedAt)}"
    )

    // Who sells, how to reach it (what its version of the seller gives), and how it is registered
    override fun seller(): InvoiceLines {
        val (contact, identifier, reach) = sale.seller.detail
        val reachLines = listOfNotNull(
            reach.phone.takeIf { it.isGiven() }?.let { "Tél. : ${it.value}" },
            reach.email.takeIf { it.isGiven() }?.let { "Email : ${it.value}" },
            reach.website.takeIf { it.isGiven() }?.let { "Site : ${it.value}" }
        )
        val identifierLines = listOf(
            "SIRET : ${identifier.siret.value}",
            "TVA intracommunautaire : ${identifier.vatNumber.value}"
        )
        val lines = listOf(contact.name.value, contact.address.value) + reachLines + identifierLines
        return InvoiceLines(*lines.toTypedArray())
    }

    // The billing address, and the email of the customer
    override fun billedTo(): InvoiceLines {
        val billingAddress = order.addresses.billing
        val email = sale.administeredOrder.customer.email.value
        val vatNumber = vatNumberOf(billingAddress.recipient)
        val lines = listOf("Facturé à") + linesOf(billingAddress) + listOfNotNull(vatNumber) + email
        return InvoiceLines(*lines.toTypedArray())
    }

    override fun deliveredTo(): InvoiceLines {
        val lines = listOf("Livré à") + linesOf(order.addresses.delivery)
        return InvoiceLines(*lines.toTypedArray())
    }

    override fun tableHeader() =
        InvoiceRow("Désignation", "Quantité", "Prix unitaire HT", "Taux TVA", "Montant TVA", "Total TTC")

    // The lines of the order, its discount, then its shipping: the unit price without VAT, the rate of VAT, the
    // VAT of the row and its price with VAT. A product follows its rate of VAT; the discount and the shipping
    // are split among the rates of the items, as the VAT of the order is, and give these rates
    override fun tableRows(): InvoiceRows {
        val productRows = order.lines.asList().map { line ->
            val unitPriceExcludingVat = VatBreakdown(line.unitPrice, line.vatRate).priceExcludingVat()
            val lineVat = VatBreakdown(line.linePrice(), line.vatRate).vat()
            InvoiceRow(
                line.productName.value, "${line.quantity.value}", priceOf(unitPriceExcludingVat),
                rateOf(line.vatRate), priceOf(lineVat), priceOf(line.linePrice())
            )
        }
        val shipping = VatOfAdjustment(order.shipping.cost, order)
        val shippingRow = InvoiceRow(
            order.shipping.method.label().value, "1", priceOf(shipping.priceExcludingVat()), ratesOfTheItems(),
            priceOf(shipping.vat()), priceOf(order.shipping.cost)
        )
        return InvoiceRows(productRows + discountRows() + shippingRow)
    }

    // The discount of the order, taken off its items, if any
    private fun discountRows(): List<InvoiceRow> {
        val discount = order.discount as? AppliedOrderDiscount ?: return emptyList()
        val amountOff = VatOfAdjustment(discount.amount, order)
        val row = InvoiceRow(
            discount.label().value, "1", "-${priceOf(amountOff.priceExcludingVat())}", ratesOfTheItems(),
            "-${priceOf(amountOff.vat())}", "-${priceOf(discount.amount)}"
        )
        return listOf(row)
    }

    // The total without VAT, the VAT of each rate, then the total with VAT
    override fun totals(): InvoiceRows {
        val totalExcludingVat = InvoiceRow("Total HT", priceOf(vatBreakdowns.priceExcludingVat()))
        val vatRows = vatBreakdowns.asList().map { breakdown ->
            InvoiceRow("TVA ${percentOf(breakdown.rate)} %", priceOf(breakdown.vat()))
        }
        val totalIncludingVat = InvoiceRow("Total TTC", priceOf(order.totalPrice()))
        return InvoiceRows(listOf(totalExcludingVat) + vatRows + totalIncludingVat)
    }

    override fun footer() = InvoiceLines(
        "Facture acquittée : la commande a été payée en ligne.",
        "Prix en euros, TVA française comprise."
    )

    private fun priceOf(price: Price) = price.formattedIn(FRENCH)

    private fun rateOf(vatRate: VatRate) = "${percentOf(vatRate)} %"

    // The rates of VAT of the items, from the highest: the one of all the items, or 20 % / 5,5 %
    private fun ratesOfTheItems() = VatRate.entries
        .filterNot { rate -> order.lines.soldAt(rate).isEmpty() }
        .joinToString(" / ") { rate -> rateOf(rate) }

    // In French, with a decimal comma: 5,5
    private fun percentOf(vatRate: VatRate): String {
        val percent = vatRate.percent.stripTrailingZeros().toPlainString()
        return percent.replace('.', ',')
    }

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

// The content of the credit note of a refunded order (in French): it cancels the invoice of the order, whose
// rows and totals it repeats, as the amounts given back to the customer. It is issued for the seller of the
// invoice
data class CreditNoteDocument(val creditNote: CreditNote, val invoiceDocument: InvoiceDocument) :
    BillingDocument by invoiceDocument {

    private val invoice get() = invoiceDocument.invoice

    private val order get() = invoiceDocument.sale.order

    override fun title() = InvoiceText("Avoir n° ${creditNote.number.value}")

    override fun dates() = InvoiceLines(
        "Date de l'avoir : ${DATE_FORMAT.format(creditNote.issuedAt.value)}",
        "Annule la facture n° ${invoice.number.value} du ${DATE_FORMAT.format(invoice.issuedAt.value)}",
        "Commande n° ${order.number()} du ${DATE_FORMAT.format(order.placedAt)}"
    )

    override fun footer() = InvoiceLines(
        "Avoir remboursé : le paiement de la commande a été rendu au client.",
        "Prix en euros, TVA française comprise."
    )
}
