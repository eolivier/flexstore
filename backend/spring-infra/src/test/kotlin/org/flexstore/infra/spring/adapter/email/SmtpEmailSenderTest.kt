package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.sharedkernel.ShopUrl
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.cart.*
import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(OutputCaptureExtension::class)
class SmtpEmailSenderTest {

    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val janeDoe = PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe")))
    private val homeStreet = Street("12 rue des Lilas")
    private val homeTown = Town(PostalCode("75011"), City("Paris"))
    private val homeLocation = Location(StreetAddress(homeStreet), Locality(homeTown, Country("France")))
    private val checkout = Checkout(customer, OrderAddresses.sameFor(PostalAddress(janeDoe, homeLocation)))
    private val orderStamp = OrderStamp(OrderId("c5f7326c-0000"), Instant.parse("2026-10-24T10:00:00Z"))
    private val orderPlacement = OrderPlacement(orderStamp, checkout)
    private val order = Order.place(orderPlacement, Cart(customer).add(lamp, Quantity(1)))
    private val email = OrderConfirmationEmail(Email("jane@example.com"), FullName(FirstName("Jane"), LastName("Doe")), order)
    private val mailSender = mock(JavaMailSender::class.java)
    private val shopUrl = ShopUrl("https://flexstore.example")
    private val adapter = SmtpEmailSender(mailSender, "Flexstore <no-reply@flexstore.com>", shopUrl)

    @Test
    fun `should log the email sent`(output: CapturedOutput) {
        // Act
        adapter.send(email)
        // Assert
        verify(mailSender).send(any(SimpleMailMessage::class.java))
        assertThat(output.out).contains("Order confirmation email of order C5F7326C sent to jane@example.com")
    }

    @Test
    fun `should log an email that could not be sent, without failing`(output: CapturedOutput) {
        // Arrange
        doThrow(MailSendException("SMTP server unreachable")).`when`(mailSender).send(any(SimpleMailMessage::class.java))
        // Act
        adapter.send(email)
        // Assert
        assertThat(output.out).contains("Order confirmation email of order C5F7326C to jane@example.com not sent")
        assertThat(output.out).doesNotContain("sent to jane@example.com")
    }
}
