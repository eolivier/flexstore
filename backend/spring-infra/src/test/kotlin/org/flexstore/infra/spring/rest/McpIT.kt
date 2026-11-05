package org.flexstore.infra.spring.rest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class McpIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun postMessage(message: String) =
        mockMvc.perform(
            post("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .content(message)
        )

    @Test
    fun `an assistant should initialize the server without signing in`() {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18"}}"""
        // Act
        val response = postMessage(message)
        // Assert
        response.andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.result.serverInfo.name").value("flexstore"))
    }

    @Test
    fun `a notification should be accepted without content`() {
        // Act
        val response = postMessage("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
        // Assert
        response.andExpect(status().isAccepted).andExpect(content().string(""))
    }

    @Test
    fun `the tool list_products should give the products of the catalog`() {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"list_products"}}"""
        // Act
        val response = postMessage(message)
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.result.isError").value(false))
            .andExpect(jsonPath("$.result.content[0].text").value(org.hamcrest.Matchers.containsString("\"sku\"")))
    }

    @Test
    fun `the tool get_product should refuse a product that does not exist`() {
        // Arrange
        val message =
            """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"get_product","arguments":{"id":"unknown"}}}"""
        // Act
        val response = postMessage(message)
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.result.isError").value(true))
            .andExpect(jsonPath("$.result.content[0].text").value("Product with ID unknown does not exist."))
    }

    @Test
    fun `the server should not open a stream`() {
        // Act
        val response = mockMvc.perform(get("/mcp"))
        // Assert
        response.andExpect(status().isMethodNotAllowed)
    }
}
