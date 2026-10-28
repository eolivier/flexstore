package org.flexstore.domain.email

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ShopUrl

// The wording of the email that tells a customer that their order is shipped, delivered, or cancelled and
// refunded (in French, the language of the shop)
class OrderStatusEmailContent(email: OrderStatusEmail, shopUrl: ShopUrl) : EmailContent {

    private val order = email.order

    private val news = when (order.status) {
        OrderStatus.SHIPPED -> "a été expédiée"
        OrderStatus.DELIVERED -> "a été livrée"
        OrderStatus.REFUNDED -> "a été annulée et remboursée"
        else -> throw IllegalArgumentException("An order ${order.status} is not announced to its customer")
    }

    private val nextStep = when (order.status) {
        OrderStatus.SHIPPED -> "Elle est en route vers votre adresse de livraison."
        OrderStatus.REFUNDED -> "Le montant payé vous est remboursé sur votre moyen de paiement, sous quelques jours."
        else -> "Nous espérons qu'elle vous donne entière satisfaction."
    }

    override val subject = EmailSubject("Flexstore - Votre commande ${order.number()} $news")

    override val body = EmailBody(buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Votre commande ${order.number()} $news. $nextStep")
        appendLine()
        appendLine("Suivez votre commande depuis votre compte : ${OrderPage.of(shopUrl, order.orderId).url}")
        appendLine()
        append("L'équipe Flexstore")
    })
}
