package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.infra.spring.adapter.jpa.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.jpa.JpaProductRepository
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
    private lateinit var productRepository: JpaProductRepository

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        cartItemRepository.deleteAll()
        productRepository.deleteAll(productRepository.findAll().filter { it.name == TEST_PRODUCT_NAME })
    }

    @Test
    fun `should read the catalog from the database`() {
        mockMvc.perform(get("/api/products/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(9))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[0].name").value("Organic Cotton T-shirt"))
            .andExpect(jsonPath("$[0].category").value("CLOTHING"))
            .andExpect(jsonPath("$[0].price").value(25))
            .andExpect(jsonPath("$[0].currency").value("€"))
    }

    @Test
    fun `should store a created product`() {
        val draft = """{"name": "$TEST_PRODUCT_NAME", "description": "Stored in PostgreSQL",
            "category": "HOME", "price": 12.5, "currency": "EUR"}"""

        mockMvc.perform(post("/api/products").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(draft))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
        mockMvc.perform(get("/api/products/"))
            .andExpect(jsonPath("$[*].name", hasItem(TEST_PRODUCT_NAME)))

        // The same product cannot be created twice, even with another price scale
        mockMvc.perform(post("/api/products").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
            .content(draft.replace("12.5", "12.50")))
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should store the cart items with the catalog product`() {
        // The product data sent by the client is ignored: the catalog one is used
        saveItem(item(productId = "4", quantity = 2, name = "Fake name", price = BigDecimal.ONE))

        mockMvc.perform(get("/api/cart/cart-items"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].productName").value("5G Smartphone"))
            .andExpect(jsonPath("$.items[0].productQuantity").value(2))
            .andExpect(jsonPath("$.items[0].itemPrice").value(798))
            .andExpect(jsonPath("$.totalItemsPrice").value(798))
    }

    @Test
    fun `should change the quantity and remove a cart item`() {
        saveItem(item(productId = "7", quantity = 1))
        // Adding the same product again changes the quantity of its line
        saveItem(item(productId = "7", quantity = 3))
        val itemId = cartItemIds().single()
        mockMvc.perform(get("/api/cart/cart-items"))
            .andExpect(jsonPath("$.items[0].productQuantity").value(3))

        saveItem(item(itemId = itemId, productId = "7", quantity = 0))

        mockMvc.perform(get("/api/cart/cart-items"))
            .andExpect(jsonPath("$.items.length()").value(0))
            .andExpect(jsonPath("$.totalItemsPrice").value(0))
    }

    @Test
    fun `should not add an unknown product to the cart`() {
        mockMvc.perform(
            post("/api/cart/save").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item(productId = "unknown", quantity = 1)))
        ).andExpect(status().isNotFound)
    }

    private fun saveItem(item: JsonItem) {
        mockMvc.perform(
            post("/api/cart/save").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
    }

    private fun cartItemIds(): List<String> {
        val body = mockMvc.perform(get("/api/cart/cart-items")).andReturn().response.contentAsString
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
