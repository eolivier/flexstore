package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.*
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FRENCH = Locale.FRANCE
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", FRENCH).withZone(ZoneId.of("Europe/Paris"))

// The wording of the order confirmation email (in French, the language of the shop)
class OrderConfirmationEmailContent(email: OrderConfirmationEmail, frontUrl: String) {

    private val order = email.order

    val subject = "Flexstore - Confirmation de votre commande ${order.number()}"

    val body = buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Merci pour votre commande ${order.number()} du ${DATE_FORMAT.format(order.placedAt)}.")
        appendLine()
        order.lines.asList().forEach { line ->
            appendLine("- ${line.quantity.value} × ${line.productName.value} : ${amount(line.linePrice())}")
        }
        appendLine()
        appendLine("Total : ${amount(order.totalPrice())}")
        appendLine()
        appendLine("Suivez votre commande depuis votre compte : ${frontUrl.trimEnd('/')}/orders/${order.orderId.value}")
        appendLine()
        append("L'équipe Flexstore")
    }

    private fun amount(price: Price): String {
        val format = NumberFormat.getNumberInstance(FRENCH).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
        return "${format.format(price.amount.value)} ${price.currency.symbol}"
    }
}
