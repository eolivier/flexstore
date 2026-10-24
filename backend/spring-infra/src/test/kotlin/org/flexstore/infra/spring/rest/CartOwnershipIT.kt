package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.domain.entity.VisitorCartId
import org.flexstore.infra.spring.adapter.jpa.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.jpa.JpaCartRepository
import org.flexstore.infra.spring.adapter.rest.CART_ID_HEADER
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

// The administration account created by the migrations: a customer cart references an existing user
private const val CUSTOMER_ID = "u-admin"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CartOwnershipIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    private val visitorCartId = VisitorCartId.nextId().value

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
    }

    @Test
    fun `a first item should create a visitor cart and return its id`() {
        // Act
        val result = mockMvc.perform(saveItem(productId = "1", quantity = 1))
        // Assert
        val cartId = objectMapper.readTree(result.andExpect(status().isOk).andReturn().response.contentAsString)["cartId"].asText()
        mockMvc.perform(get("/api/cart/cart-items").header(CART_ID_HEADER, cartId))
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `visitors should not see the cart of another visitor`() {
        // Arrange
        mockMvc.perform(saveItem(productId = "1", quantity = 1).header(CART_ID_HEADER, visitorCartId))
        // Act
        val result = mockMvc.perform(get("/api/cart/cart-items").header(CART_ID_HEADER, VisitorCartId.nextId().value))
        // Assert
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(0))
    }

    @Test
    fun `a visitor without cart id should get an empty cart`() {
        // Act
        val result = mockMvc.perform(get("/api/cart/cart-items"))
        // Assert
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(0))
    }

    @Test
    fun `a customer should find their cart with their token`() {
        // Arrange
        mockMvc.perform(saveItem(productId = "4", quantity = 2).with(asUser(CUSTOMER_ID))).andExpect(status().isOk)
        // Act
        val result = mockMvc.perform(get("/api/cart/cart-items").with(asUser(CUSTOMER_ID)))
        // Assert
        result.andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].productName").value("5G Smartphone"))
            .andExpect(jsonPath("$.items[0].productQuantity").value(2))
    }

    @Test
    fun `merging should require a signed-in customer`() {
        // Act
        val result = mockMvc.perform(post("/api/cart/merge").header(CART_ID_HEADER, visitorCartId))
        // Assert
        result.andExpect(status().isUnauthorized)
    }

    @Test
    fun `merging should move the visitor items into the customer cart`() {
        // Arrange
        mockMvc.perform(saveItem(productId = "7", quantity = 1).with(asUser(CUSTOMER_ID))).andExpect(status().isOk)
        mockMvc.perform(saveItem(productId = "7", quantity = 2).header(CART_ID_HEADER, visitorCartId)).andExpect(status().isOk)
        mockMvc.perform(saveItem(productId = "9", quantity = 1).header(CART_ID_HEADER, visitorCartId)).andExpect(status().isOk)
        // Act
        val result = mockMvc.perform(post("/api/cart/merge").header(CART_ID_HEADER, visitorCartId).with(asUser(CUSTOMER_ID)))
        // Assert
        result.andExpect(status().isNoContent)
        mockMvc.perform(get("/api/cart/cart-items").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[?(@.productId == '7')].productQuantity").value(2))
            .andExpect(jsonPath("$.items[?(@.productId == '9')].productQuantity").value(1))
        mockMvc.perform(get("/api/cart/cart-items").header(CART_ID_HEADER, visitorCartId))
            .andExpect(jsonPath("$.items.length()").value(0))
    }

    private fun saveItem(productId: String, quantity: Int): MockHttpServletRequestBuilder {
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
        return post("/api/cart/save").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(item))
    }
}
