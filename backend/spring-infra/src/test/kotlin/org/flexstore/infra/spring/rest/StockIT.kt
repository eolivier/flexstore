package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-stock-customer"

// Product 5 of the catalog (10" Tablet): its stock is given by the tests, then put back to 100
private const val PRODUCT_ID = "5"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StockIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-stock-customer@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        changeStock(100)
        orderRepository.deleteAll(orderRepository.findAllByUserId(CUSTOMER_ID))
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        if (userRepository.existsById(CUSTOMER_ID)) userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `everyone should see the stock of a product`() {
        // Arrange
        changeStock(7)
        // Act
        val response = mockMvc.perform(get("/api/stocks/$PRODUCT_ID"))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.quantity").value(7))
    }

    @Test
    fun `only an administrator should give the stock of a product`() {
        // Act
        val response = mockMvc.perform(
            put("/api/admin/stocks/$PRODUCT_ID").with(asUser(CUSTOMER_ID))
                .contentType(MediaType.APPLICATION_JSON).content("""{"quantity": 3}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a negative stock should be refused`() {
        // Act
        val response = changeStock(-1)
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `an order asking for more than the stock should be refused`() {
        // Arrange
        changeStock(1)
        addToCart(quantity = 2)
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `a paid order should take its units out of the stock`() {
        // Arrange
        changeStock(5)
        addToCart(quantity = 2)
        val body = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        val orderId = objectMapper.readTree(body)["id"].asText()
        // Act
        mockMvc.pay(orderId, CUSTOMER_ID).andExpect(status().isCreated)
        // Assert
        mockMvc.perform(get("/api/stocks/$PRODUCT_ID")).andExpect(jsonPath("$.quantity").value(3))
    }

    private fun changeStock(quantity: Int) = mockMvc.perform(
        put("/api/admin/stocks/$PRODUCT_ID").with(asAdmin())
            .contentType(MediaType.APPLICATION_JSON).content("""{"quantity": $quantity}""")
    )

    private fun addToCart(quantity: Int) {
        val item = JsonItem("", PRODUCT_ID, "Product", "Description", "ELECTRONICS", BigDecimal.TEN, "€", quantity)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }
}
