package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-invoice-customer"
private const val OTHER_CUSTOMER_ID = "it-invoice-other-customer"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class InvoiceIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @BeforeEach
    fun createCustomers() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-invoice-customer@example.com", "not-used"))
        userRepository.save(UserEntity(OTHER_CUSTOMER_ID, "John", "Smith", "it-invoice-other@example.com", "x"))
    }

    @AfterEach
    fun cleanUp() {
        listOf(CUSTOMER_ID, OTHER_CUSTOMER_ID).forEach { customerId ->
            jdbcTemplate.deleteOrdersOf(customerId)
            cartRepository.findByUserId(customerId)?.let { cart ->
                cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
                cartRepository.delete(cart)
            }
            if (userRepository.existsById(customerId)) userRepository.deleteById(customerId)
        }
    }

    @Test
    fun `a customer should download the invoice of their paid order, as a PDF`() {
        // Arrange
        val orderId = placeOrder(paid = true)
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId/invoice").with(asUser(CUSTOMER_ID)))
        // Assert
        val pdf = response.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"facture-${orderId.take(8).uppercase()}.pdf\""))
            .andReturn().response.contentAsByteArray
        assertThat(String(pdf.copyOf(5))).isEqualTo("%PDF-")
    }

    @Test
    fun `an order not paid should have no invoice`() {
        // Arrange
        val orderId = placeOrder(paid = false)
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId/invoice").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `the invoice of another customer should not be found`() {
        // Arrange
        val orderId = placeOrder(paid = true)
        // Act
        val response = mockMvc.perform(get("/api/orders/$orderId/invoice").with(asUser(OTHER_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `an administrator should download the invoice of any paid order`() {
        // Arrange
        val orderId = placeOrder(paid = true)
        // Act
        val response = mockMvc.perform(get("/api/admin/orders/$orderId/invoice").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk).andExpect(content().contentType(MediaType.APPLICATION_PDF))
    }

    @Test
    fun `a paid order should be invoiced with the next number of the sequence`() {
        // Arrange
        val firstOrderId = placeOrder(paid = true)
        // Act
        val secondOrderId = placeOrder(paid = true)
        // Assert
        val numbers = listOf(firstOrderId, secondOrderId).map { orderId ->
            jdbcTemplate.queryForObject("SELECT number FROM invoice WHERE customer_order_id = ?", String::class.java, orderId)!!
        }
        val ranks = numbers.map { it.substringAfter('-').toInt() }
        assertThat(ranks[1]).isEqualTo(ranks[0] + 1)
    }

    // Without a Stripe key, the payment is simulated: the order is paid at once, and invoiced
    private fun placeOrder(paid: Boolean): String {
        val item = JsonItem("", "1", "Product", "Description", "CLOTHING", BigDecimal.TEN, "€", 1)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        val orderId = objectMapper.readTree(body)["id"].asText()
        if (paid) mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        return orderId
    }
}
