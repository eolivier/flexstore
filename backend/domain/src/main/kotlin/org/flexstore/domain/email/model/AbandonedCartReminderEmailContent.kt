package org.flexstore.domain.email.model

import org.flexstore.domain.sharedkernel.ShopUrl
import java.util.Locale

private val FRENCH = Locale.FRANCE

// The wording of the abandoned cart reminder email (in French, the language of the shop)
class AbandonedCartReminderEmailContent(email: AbandonedCartReminderEmail, shopUrl: ShopUrl) : EmailContent {

    private val items = email.cart.items

    override val subject = EmailSubject("Flexstore - Vous avez oublié des articles dans votre panier")

    override val body = EmailBody(buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Votre panier vous attend. Vous y avez laissé :")
        appendLine()
        items.fold(Unit) { _, product, quantity ->
            appendLine("- ${quantity.value} × ${product.name.value}")
        }
        appendLine()
        appendLine("Total : ${items.totalPrice().formattedIn(FRENCH)}")
        appendLine()
        appendLine("Retrouvez votre panier et finalisez votre commande : ${shopUrl.pageAt("cart")}")
        appendLine()
        append("L'équipe Flexstore")
    })
}
