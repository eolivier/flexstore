package org.flexstore.infra.spring.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest(properties = ["flexstore.mcp.enabled=false"])
@AutoConfigureMockMvc
class McpDisabledIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `the MCP server should not be served when it is turned off`() {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":1,"method":"ping"}"""
        // Act
        val response = mockMvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON).content(message))
        // Assert
        response.andExpect(status().isNotFound)
    }
}
