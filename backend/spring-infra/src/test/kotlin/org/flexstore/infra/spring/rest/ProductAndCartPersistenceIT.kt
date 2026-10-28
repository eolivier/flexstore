package org.flexstore.infra.spring.rest

import org.flexstore.domain.cart.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaProductRepository
import org.flexstore.infra.spring.adapter.rest.CART_ID_HEADER
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asAdmin
import org.hamcrest.Matchers.hasItem
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val TEST_PRODUCT_NAME = "Persistence IT product"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductAndCartPersistenceIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var productRepository: JpaProductRepository

    // The cart of a visitor
    private val cartId = VisitorCartId.nextId().value

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        productRepository.deleteAll(productRepository.findAll().filter { it.name == TEST_PRODUCT_NAME })
    }

    @Test
    fun `should read the catalog from the database`() {
        // Act
        val response = mockMvc.perform(get("/api/products/"))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(9))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].name").value("Organic Cotton T-shirt"))
            .andExpect(jsonPath("$[0].category").value("CLOTHING"))
            .andExpect(jsonPath("$[0].price").value(25))
            .andExpect(jsonPath("$[0].currency").value("€"))
    }

    @Test
    fun `should store a created product`() {
        // Arrange
        val draft = draftProduct(price = "12.5")
        // Act
        val response = createProduct(draft)
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
        mockMvc.perform(get("/api/products/"))
            .andExpect(jsonPath("$[*].name", hasItem(TEST_PRODUCT_NAME)))
    }

    @Test
    fun `should not create the same product twice, even with another price scale`() {
        // Arrange
        createProduct(draftProduct(price = "12.5")).andExpect(status().isCreated)
        // Act
        val response = createProduct(draftProduct(price = "12.50"))
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should store the cart items with the catalog product`() {
        // Arrange
        val itemWithFakeProductData = item(productId = "4", quantity = 2, name = "Fake name", price = BigDecimal.ONE)
        // Act
        saveItem(itemWithFakeProductData)
        // Assert
        getCart()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].productName").value("5G Smartphone"))
            .andExpect(jsonPath("$.items[0].productQuantity").value(2))
            .andExpect(jsonPath("$.items[0].itemPrice").value(798))
            .andExpect(jsonPath("$.totalItemsPrice").value(798))
    }

    @Test
    fun `adding a product already in the cart should change the quantity of its item`() {
        // Arrange
        saveItem(item(productId = "7", quantity = 1))
        // Act
        saveItem(item(productId = "7", quantity = 3))
        // Assert
        getCart()
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].productQuantity").value(3))
    }

    @Test
    fun `a quantity of zero should remove the cart item`() {
        // Arrange
        saveItem(item(productId = "7", quantity = 1))
        val itemId = cartItemIds().single()
        // Act
        saveItem(item(itemId = itemId, productId = "7", quantity = 0))
        // Assert
        getCart()
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalItemsPrice").value(0))
    }

    @Test
    fun `should not add an unknown product to the cart`() {
        // Arrange
        val unknownProductItem = item(productId = "unknown", quantity = 1)
        // Act
        val response = mockMvc.perform(
            post("/api/cart/save").header(CART_ID_HEADER, cartId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unknownProductItem))
        )
        // Assert
        response.andExpect(status().isNotFound)
    }

    private fun draftProduct(price: String) =
        """{"category": "HOME", "price": $price, "currency": "EUR", "translations": [
        {"language": "en", "name": "$TEST_PRODUCT_NAME", "description": "Stored in PostgreSQL"}]}"""

    private fun createProduct(draft: String) =
        mockMvc.perform(post("/api/products").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(draft))

    private fun saveItem(item: JsonItem) {
        mockMvc.perform(
            post("/api/cart/save").header(CART_ID_HEADER, cartId).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }

    private fun getCart() = mockMvc.perform(get("/api/cart/cart-items").header(CART_ID_HEADER, cartId))

    private fun cartItemIds(): List<String> {
        val body = getCart().andReturn().response.contentAsString
        return objectMapper.readTree(body)["items"].map { it["itemId"].asText() }
    }

    private fun item(
        itemId: String = "",
        productId: String,
        quantity: Int,
        name: String = "Product",
        price: BigDecimal = BigDecimal.TEN
    ) = JsonItem(
        itemId = itemId,
        productId = productId,
        productName = name,
        productDescription = "Description",
        productCategory = "ELECTRONICS",
        productPrice = price,
        productCurrency = "€",
        productQuantity = quantity
    )
}
