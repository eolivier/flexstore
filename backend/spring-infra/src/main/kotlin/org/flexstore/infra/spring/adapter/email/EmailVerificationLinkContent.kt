package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRANCE).withZone(ZoneId.of("Europe/Paris"))

// The wording of the email with the verification link (in French, the language of the shop)
class EmailVerificationLinkContent(link: EmailVerificationLink) {

    val subject = "Flexstore - Confirmez votre adresse email"

    val body = buildString {
        appendLine("Bonjour ${link.customerName.value},")
        appendLine()
        appendLine("Merci d'avoir créé votre compte Flexstore. Confirmez votre adresse email en ouvrant ce lien :")
        appendLine()
        appendLine(link.link().value)
        appendLine()
        appendLine("Ce lien est valable 1 heure (jusqu'à ${TIME_FORMAT.format(link.expiresAt.value)}). Passé ce délai, demandez un nouveau lien depuis votre compte.")
        appendLine()
        append("L'équipe Flexstore")
    }
}
