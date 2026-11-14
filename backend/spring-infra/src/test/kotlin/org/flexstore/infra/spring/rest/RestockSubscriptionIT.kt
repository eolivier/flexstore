package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.restock.RestockNotificationService
import org.flexstore.infra.spring.adapter.email.RecordingEmailConfig
import org.flexstore.infra.spring.adapter.email.RecordingEmailSender
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "it-restock-customer"
private const val OTHER_CUSTOMER_ID = "it-restock-other-customer"
private const val PRODUCT_ID = "1"
private const val ENOUGH_UNITS = 10

// The stocks are refilled before each test (RefilledStocks): the product of the tests is emptied by the test
@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(RecordingEmailConfig::class)
class RestockSubscriptionIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var restockNotificationService: RestockNotificationService

    @Autowired
    private lateinit var emailSender: RecordingEmailSender

    @BeforeEach
    fun createCustomers() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-restock-customer@example.com", "unused"))
        userRepository.save(UserEntity(OTHER_CUSTOMER_ID, "John", "Roe", "it-restock-other@example.com", "unused"))
        changeStock(PRODUCT_ID, 0)
        emailSender.clear()
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.update(
            "DELETE FROM restock_subscription WHERE user_id IN (?, ?)", CUSTOMER_ID, OTHER_CUSTOMER_ID
        )
        listOf(CUSTOMER_ID, OTHER_CUSTOMER_ID).forEach {
            if (userRepository.existsById(it)) userRepository.deleteById(it)
        }
        emailSender.clear()
    }

    @Test
    fun `a customer should subscribe to the return of a product out of stock`() {
        // Act
        val response = subscribe(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
        subscriptions(CUSTOMER_ID).andExpect(jsonPath("$[0]").value(PRODUCT_ID))
    }

    @Test
    fun `subscribing again should leave the subscriptions as they are`() {
        // Arrange
        subscribe(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = subscribe(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
        subscriptions(CUSTOMER_ID).andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `a customer should not subscribe to a product in stock`() {
        // Arrange
        changeStock(PRODUCT_ID, ENOUGH_UNITS)
        // Act
        val response = subscribe(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `a customer should not subscribe to an unknown product`() {
        // Act
        val response = subscribe("unknown-product", CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `the subscriptions should be the ones of their customer only`() {
        // Arrange
        subscribe(PRODUCT_ID, OTHER_CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = subscriptions(CUSTOMER_ID)
        // Assert
        response.andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `a customer should unsubscribe from the return of a product`() {
        // Arrange
        subscribe(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        val response = unsubscribe(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
        subscriptions(CUSTOMER_ID).andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `unsubscribing from a product not waited for should leave the subscriptions as they are`() {
        // Act
        val response = unsubscribe(PRODUCT_ID, CUSTOMER_ID)
        // Assert
        response.andExpect(status().isNoContent)
    }

    @Test
    fun `unsubscribing should leave the subscriptions of the other customers`() {
        // Arrange
        subscribe(PRODUCT_ID, OTHER_CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        unsubscribe(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Assert
        subscriptions(OTHER_CUSTOMER_ID).andExpect(jsonPath("$[0]").value(PRODUCT_ID))
    }

    @Test
    fun `a visitor should not unsubscribe`() {
        // Act
        val response = mockMvc.perform(delete("/api/restock-subscriptions/$PRODUCT_ID"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `a visitor should not subscribe`() {
        // Act
        val response = mockMvc.perform(put("/api/restock-subscriptions/$PRODUCT_ID"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `a visitor should not read the subscriptions`() {
        // Act
        val response = mockMvc.perform(get("/api/restock-subscriptions"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    @Test
    fun `the customer should be notified by email once the product is back in stock`() {
        // Arrange
        subscribe(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        changeStock(PRODUCT_ID, ENOUGH_UNITS)
        // Act
        restockNotificationService.notifyRestockedProducts()
        // Assert
        val recipients = emailSender.sentRestockEmails().map { it.recipient.value }
        assertThat(recipients).containsExactly("it-restock-customer@example.com")
        subscriptions(CUSTOMER_ID).andExpect(jsonPath("$").isEmpty)
    }

    @Test
    fun `the customer should not be notified while the product is out of stock`() {
        // Arrange
        subscribe(PRODUCT_ID, CUSTOMER_ID).andExpect(status().isNoContent)
        // Act
        restockNotificationService.notifyRestockedProducts()
        // Assert
        assertThat(emailSender.sentRestockEmails()).isEmpty()
        subscriptions(CUSTOMER_ID).andExpect(jsonPath("$[0]").value(PRODUCT_ID))
    }

    private fun changeStock(productId: String, quantity: Int) {
        jdbcTemplate.update("UPDATE product SET stock_quantity = ? WHERE id = ?", quantity, productId)
    }

    private fun subscribe(productId: String, customerId: String) =
        mockMvc.perform(put("/api/restock-subscriptions/$productId").with(asUser(customerId)))

    private fun unsubscribe(productId: String, customerId: String) =
        mockMvc.perform(delete("/api/restock-subscriptions/$productId").with(asUser(customerId)))

    private fun subscriptions(customerId: String) =
        mockMvc.perform(get("/api/restock-subscriptions").with(asUser(customerId))).andExpect(status().isOk)
}
