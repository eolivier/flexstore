package org.flexstore.domain.email

import org.flexstore.domain.order.OrderPage
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FRENCH = Locale.FRANCE
private val DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", FRENCH).withZone(ZoneId.of("Europe/Paris"))

// The wording of the order confirmation email (in French, the language of the shop)
class OrderConfirmationEmailContent(email: OrderConfirmationEmail, shopUrl: ShopUrl) : EmailContent {

    private val order = email.order

    override val subject = EmailSubject("Flexstore - Confirmation de votre commande ${order.number()}")

    override val body = EmailBody(buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Merci pour votre commande ${order.number()} du ${DATE_FORMAT.format(order.placedAt)}.")
        appendLine()
        order.lines.asList().forEach { line ->
            appendLine("- ${line.quantity.value} × ${line.productName.value} : ${line.linePrice().formattedIn(FRENCH)}")
        }
        appendLine()
        appendLine("Total : ${order.totalPrice().formattedIn(FRENCH)}")
        appendLine()
        appendLine("Suivez votre commande depuis votre compte : ${OrderPage.of(shopUrl, order.orderId).url}")
        appendLine()
        append("L'équipe Flexstore")
    })
}
