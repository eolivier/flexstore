package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
}
