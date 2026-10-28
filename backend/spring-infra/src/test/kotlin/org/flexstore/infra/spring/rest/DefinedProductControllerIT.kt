package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaProductRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
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

    @ParameterizedTest(name = "with Accept-Language \"{0}\", the jacket is called {1}")
    @CsvSource(
        "fr, Veste imperméable",
        "'fr-FR,fr;q=0.9,en;q=0.8', Veste imperméable",
        "'de-DE,fr;q=0.5', Veste imperméable",
        "en, Waterproof Jacket",
        "de, Waterproof Jacket",
    )
    fun `a product should be returned in the language of the customer`(acceptLanguage: String, name: String) {
        // Act
        val response = mockMvc.perform(get("/api/products/2").header(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.name").value(name))
    }

    @Test
    fun `a product should be returned in the default language without Accept-Language`() {
        // Act
        val response = mockMvc.perform(get("/api/products/2"))
        // Assert
        response.andExpect(jsonPath("$.name").value("Waterproof Jacket"))
            .andExpect(jsonPath("$.translations").doesNotExist())
    }

    @Test
    fun `the products should be returned in the language of the customer`() {
        // Act
        val response = mockMvc.perform(get("/api/products/").header(HttpHeaders.ACCEPT_LANGUAGE, "fr"))
        // Assert
        response.andExpect(jsonPath("$[?(@.id == '2')].name").value("Veste imperméable"))
            .andExpect(jsonPath("$[?(@.id == '2')].description").value("Résiste à la pluie et au vent"))
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
    fun `an administrator should create a product with its French translation`() {
        // Arrange
        val frenchLamp = """{"language": "fr", "name": "Lampe de lecture", "description": "Lumière chaude"}"""
        // Act
        val productId = createProduct("Reading lamp", frenchLamp)
        // Assert
        mockMvc.perform(get("/api/products/$productId"))
            .andExpect(jsonPath("$.translations[?(@.language == 'fr')].name").value("Lampe de lecture"))
    }

    @Test
    fun `an administrator should remove a translation by not giving it`() {
        // Arrange
        val frenchLamp = """{"language": "fr", "name": "Lampe de lecture", "description": "Lumière chaude"}"""
        val productId = createProduct("Reading lamp", frenchLamp)
        // Act
        val response = mockMvc.perform(
            put("/api/products/$productId").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
                .content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.translations.length()").value(1))
            .andExpect(jsonPath("$.translations[0].language").value("en"))
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
            put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                .content(editedProduct("Bedside lamp", 42))
        )
        // Assert
        response.andExpect(status().isUnauthorized)
    }

    private fun englishTranslation(name: String, description: String) =
        """{"language": "en", "name": "$name", "description": "$description"}"""

    private fun editedProduct(name: String, price: Int, vararg otherTranslations: String): String {
        val translations = (listOf(englishTranslation(name, "Soft light")) + otherTranslations).joinToString()
        return """{"category": "BOOKS", "price": $price, "currency": "EUR", "translations": [$translations]}"""
    }

    private fun createProduct(name: String, vararg otherTranslations: String): String {
        val english = englishTranslation("$name ${System.nanoTime()}", "Warm light")
        val translations = (listOf(english) + otherTranslations).joinToString()
        val draft = """{"category": "HOME", "price": 30, "currency": "EUR", "translations": [$translations]}"""
        val creation = post("/api/products").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(draft)
        val body = mockMvc.perform(creation)
            .andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText().also { createdProductIds.add(it) }
    }
}
