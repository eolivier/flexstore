package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.user.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRANCE).withZone(ZoneId.of("Europe/Paris"))

// The wording of the email with the verification link (in French, the language of the shop)
class EmailVerificationEmailContent(email: EmailVerificationEmail, frontUrl: String) {

    val link = "${frontUrl.trimEnd('/')}/verify-email?token=${email.token.value}"

    val subject = "Flexstore - Confirmez votre adresse email"

    val body = buildString {
        appendLine("Bonjour ${email.customerName.value},")
        appendLine()
        appendLine("Merci d'avoir créé votre compte Flexstore. Confirmez votre adresse email en ouvrant ce lien :")
        appendLine()
        appendLine(link)
        appendLine()
        appendLine("Ce lien est valable 1 heure (jusqu'à ${TIME_FORMAT.format(email.expiresAt.value)}). Passé ce délai, demandez un nouveau lien depuis votre compte.")
        appendLine()
        append("L'équipe Flexstore")
    }
}
