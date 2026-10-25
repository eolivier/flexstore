package org.flexstore.domain.email

import org.flexstore.domain.sharedkernel.ShopUrl
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH'h'mm", Locale.FRANCE).withZone(ZoneId.of("Europe/Paris"))

// The wording of the email with the link to choose a new password (in French, the language of the shop)
class PasswordResetEmailContent(email: PasswordResetEmail, shopUrl: ShopUrl) : EmailContent {

    override val subject = EmailSubject("Flexstore - Choisissez un nouveau mot de passe")

    private val expiringToken = email.reset.expiringToken
    private val link = PasswordResetPage.of(shopUrl).linkFor(expiringToken.token)
    private val validity = PasswordReset.VALIDITY.toHours()
    private val until = TIME_FORMAT.format(expiringToken.expiresAt.value)

    override val body = EmailBody(buildString {
        appendLine("Bonjour ${email.user.fullName.value},")
        appendLine()
        appendLine("Vous avez demandé à changer le mot de passe de votre compte Flexstore.")
        appendLine("Choisissez-en un nouveau en ouvrant ce lien :")
        appendLine()
        appendLine(link.value)
        appendLine()
        appendLine("Ce lien est valable $validity heure${if (validity > 1) "s" else ""} (jusqu'à $until).")
        appendLine("Si vous n'avez rien demandé, ignorez cet email : votre mot de passe ne change pas.")
        appendLine()
        append("L'équipe Flexstore")
    })
}
