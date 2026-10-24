package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.jpa.JpaProductRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DefinedProductControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var productRepository: JpaProductRepository

    // The products created by a test, deleted after it: the other tests read the seeded catalog
    private val createdProductIds = mutableListOf<String>()

    @AfterEach
    fun deleteCreatedProducts() {
        productRepository.deleteAllById(createdProductIds)
    }

    @Test
    fun `should return the products`() {
        // Act
        val response = mockMvc.perform(get("/api/products/"))
        // Assert
        val responseContent = response.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.contentAsString
        val products: List<JsonProduct> = objectMapper.readValue(
            responseContent,
            objectMapper.typeFactory.constructCollectionType(List::class.java, JsonProduct::class.java)
        )
        assertThat(products).isNotEmpty
    }

    @Test
    fun `should return a product by id`() {
        // Act
        val response = mockMvc.perform(get("/api/products/1"))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.name").isNotEmpty)
    }

    @Test
    fun `should return 404 for an unknown product`() {
        // Act
        val response = mockMvc.perform(get("/api/products/unknown"))
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `an administrator should edit a product`() {
        // Arrange
        val productId = createProduct("Reading lamp")
        // Act
        val response = mockMvc.perform(
            put("/api/products/$productId").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
                .content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.name").value("Bedside lamp"))
        mockMvc.perform(get("/api/products/$productId"))
            .andExpect(jsonPath("$.name").value("Bedside lamp"))
            .andExpect(jsonPath("$.description").value("Soft light"))
            .andExpect(jsonPath("$.category").value("BOOKS"))
            .andExpect(jsonPath("$.price").value(42))
    }

    @Test
    fun `should return 404 when editing an unknown product`() {
        // Act
        val response = mockMvc.perform(
            put("/api/products/unknown").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
                .content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `should forbid the edition of a product to a user without the admin role`() {
        // Act
        val response = mockMvc.perform(
            put("/api/products/1").with(asUser("a-customer")).contentType(MediaType.APPLICATION_JSON)
                .content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `should reject the anonymous edition of a product`() {
        // Act
        val response = mockMvc.perform(
            put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    private fun editedProduct(name: String, price: Int) =
        """{"name": "$name", "description": "Soft light", "category": "BOOKS", "price": $price, "currency": "EUR"}"""

    private fun createProduct(name: String): String {
        val draft = """{"name": "$name ${System.nanoTime()}", "description": "Warm light", "category": "HOME", "price": 30, "currency": "EUR"}"""
        val body = mockMvc.perform(post("/api/products").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(draft))
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText().also { createdProductIds.add(it) }
    }
}
