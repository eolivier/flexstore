package org.flexstore.domain.email.model

import org.flexstore.domain.sharedkernel.ShopUrl

// The wording of the restock email (in French, the language of the shop)
class RestockEmailContent(email: RestockEmail, shopUrl: ShopUrl) : EmailContent {

    private val productName = email.product.name.value
    private val productPage = shopUrl.pageAt("products/${email.product.productId.id.value}")

    override val subject = EmailSubject("Flexstore - $productName est de nouveau en stock")

    override val body = EmailBody(buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Bonne nouvelle : $productName, que vous attendiez, est de nouveau en stock.")
        appendLine()
        appendLine("Découvrez-le dès maintenant : $productPage")
        appendLine()
        append("L'équipe Flexstore")
    })
}
