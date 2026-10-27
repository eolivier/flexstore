package org.flexstore.infra.spring.rest

import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.email.RecordingEmailConfig
import org.flexstore.infra.spring.adapter.email.RecordingEmailSender
import org.flexstore.domain.email.OrderConfirmationEmailContent
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

// The administration account created by the migrations
private const val CUSTOMER_ID = "u-admin"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(RecordingEmailConfig::class)
class OrderConfirmationEmailIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var emailSender: RecordingEmailSender

    @Autowired
    private lateinit var shopSettingsRepository: ShopSettingsRepository

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        emailSender.clear()
        shopSettingsRepository.save(ShopSettings.byDefault())
        orderRepository.deleteAll(orderRepository.findAllByUserId(CUSTOMER_ID))
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
    }

    @ParameterizedTest(name = "order confirmation email {0} -> {1} email(s) sent")
    @CsvSource("ENABLED, 1", "DISABLED, 0")
    fun `paying an order should send the confirmation email when the settings enable it`(
        activation: Activation,
        sentCount: Int
    ) {
        // Arrange
        shopSettingsRepository.save(ShopSettings(orderConfirmationEmail = activation))
        val orderId = placeOrder()
        // Act
        val response = mockMvc.pay(orderId, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isCreated)
        assertThat(emailSender.sentEmails()).hasSize(sentCount)
    }

    @Test
    fun `an order awaiting its payment should not be confirmed by email`() {
        // Act
        placeOrder()
        // Assert
        assertThat(emailSender.sentEmails()).isEmpty()
    }

    @Test
    fun `the confirmation email should be sent to the customer and list the order`() {
        // Arrange
        val orderId = placeOrder()
        // Act
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        // Assert
        val email = emailSender.sentEmails().single()
        val content = OrderConfirmationEmailContent(email, ShopUrl("https://flexstore.example"))
        assertThat(email.recipient).isEqualTo(Email("admin@flexstore.com"))
        assertThat(content.subject.value).isEqualTo("Flexstore - Confirmation de votre commande ${email.order.number()}")
        assertThat(content.body.value).contains("2 × Laptop : 1\u202F598,00 €", "https://flexstore.example/orders/${email.order.orderId.value}")
    }

    private fun placeOrder(): String {
        addToCart(productId = "6", quantity = 2)
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText()
    }

    private fun addToCart(productId: String, quantity: Int) {
        val item = JsonItem(
            itemId = "",
            productId = productId,
            productName = "Product",
            productDescription = "Description",
            productCategory = "ELECTRONICS",
            productPrice = BigDecimal.TEN,
            productCurrency = "€",
            productQuantity = quantity
        )
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }
}
