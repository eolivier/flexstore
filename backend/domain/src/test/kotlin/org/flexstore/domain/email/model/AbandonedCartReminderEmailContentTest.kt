package org.flexstore.domain.email.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AbandonedCartReminderEmailContentTest : WithAssertions {

    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cart = Cart(customer).add(deskLamp, Quantity(2))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val shopUrl = ShopUrl("https://flexstore.example/")
    private val email = AbandonedCartReminderEmail(Email("jane@example.com"), janeDoe, cart)

    @Test
    fun `the email should greet the customer and list the items left in the cart`() {
        // Act
        val content = AbandonedCartReminderEmailContent(email, shopUrl)
        // Assert
        assertThat(content.body.value).contains("Bonjour Jane Doe,")
        assertThat(content.body.value).contains("- 2 × Desk lamp")
        assertThat(content.body.value).contains("Total : 70,00 €")
    }

    @Test
    fun `the email should lead back to the cart of the shop`() {
        // Act
        val content = AbandonedCartReminderEmailContent(email, shopUrl)
        // Assert
        assertThat(content.body.value).contains("https://flexstore.example/cart")
    }
}
