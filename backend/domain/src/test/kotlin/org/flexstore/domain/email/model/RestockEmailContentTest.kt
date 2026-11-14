package org.flexstore.domain.email.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RestockEmailContentTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val shopUrl = ShopUrl("https://flexstore.example/")
    private val email = RestockEmail(Email("jane@example.com"), janeDoe, deskLamp)

    @Test
    fun `the subject should name the product back in stock`() {
        // Act
        val content = RestockEmailContent(email, shopUrl)
        // Assert
        assertThat(content.subject.value).isEqualTo("Flexstore - Desk lamp est de nouveau en stock")
    }

    @Test
    fun `the email should greet the customer and name the product`() {
        // Act
        val content = RestockEmailContent(email, shopUrl)
        // Assert
        assertThat(content.body.value).contains("Bonjour Jane Doe,")
        assertThat(content.body.value).contains("Desk lamp, que vous attendiez, est de nouveau en stock")
    }

    @Test
    fun `the email should lead to the page of the product`() {
        // Act
        val content = RestockEmailContent(email, shopUrl)
        // Assert
        assertThat(content.body.value).contains("https://flexstore.example/products/desk-lamp")
    }
}
