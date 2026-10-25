package org.flexstore.domain.email

data class EmailSubject(val value: String)

data class EmailBody(val value: String)

// What an email of the shop says to the customer, as specified by the product owner (in French, the language of
// the shop); the infrastructure only sends it
interface EmailContent {
    val subject: EmailSubject
    val body: EmailBody
}
