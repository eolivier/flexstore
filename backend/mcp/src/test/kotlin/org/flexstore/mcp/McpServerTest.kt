package org.flexstore.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class McpServerTest {
    private val json = ObjectMapper()
    private val mcpServer = McpServer(ProductTools(productUseCasesOf(deskLamp)))

    private fun McpAnswer.reply(): JsonNode = json.readTree((this as McpAnswer.Reply).json)

    @ParameterizedTest(name = "the protocol {0} is answered with {1}")
    @CsvSource("2024-11-05, 2024-11-05", "2025-03-26, 2025-03-26", "2025-06-18, 2025-06-18", "1999-01-01, 2025-06-18")
    fun `initializing should agree on a protocol version the server supports`(asked: String, agreed: String) {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"$asked"}}"""
        // Act
        val answer = mcpServer.answer(message)
        // Assert
        val reply = answer.reply()
        assertThat(reply["id"].asInt()).isEqualTo(1)
        assertThat(reply["result"]["protocolVersion"].asText()).isEqualTo(agreed)
        assertThat(reply["result"]["capabilities"]["tools"]["listChanged"].asBoolean()).isFalse()
        assertThat(reply["result"]["serverInfo"]["name"].asText()).isEqualTo("flexstore")
    }

    @Test
    fun `a notification should be accepted without a reply`() {
        // Act
        val answer = mcpServer.answer("""{"jsonrpc":"2.0","method":"notifications/initialized"}""")
        // Assert
        assertThat(answer).isEqualTo(McpAnswer.Accepted)
    }

    @Test
    fun `a reply of the client should be accepted without a reply`() {
        // Act
        val answer = mcpServer.answer("""{"jsonrpc":"2.0","id":7,"result":{}}""")
        // Assert
        assertThat(answer).isEqualTo(McpAnswer.Accepted)
    }

    @Test
    fun `a ping should be answered with an empty result`() {
        // Act
        val answer = mcpServer.answer("""{"jsonrpc":"2.0","id":"ping-1","method":"ping"}""")
        // Assert
        val reply = answer.reply()
        assertThat(reply["id"].asText()).isEqualTo("ping-1")
        assertThat(reply["result"].isEmpty).isTrue()
    }

    @Test
    fun `the tools should be listed`() {
        // Act
        val answer = mcpServer.answer("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
        // Assert
        val toolNames = answer.reply()["result"]["tools"].map { it["name"].asText() }
        assertThat(toolNames).containsExactly("list_products", "get_product")
    }

    @Test
    fun `a tool call should give the products of the domain as a text`() {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"list_products"}}"""
        // Act
        val answer = mcpServer.answer(message)
        // Assert
        val result = answer.reply()["result"]
        assertThat(result["isError"].asBoolean()).isFalse()
        assertThat(result["content"][0]["type"].asText()).isEqualTo("text")
        assertThat(result["content"][0]["text"].asText()).contains("\"id\" : \"desk-lamp\"")
    }

    @Test
    fun `a tool call refused by the domain should be an error of the tool, not of the protocol`() {
        // Arrange
        val message = """{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"get_product","arguments":{}}}"""
        // Act
        val answer = mcpServer.answer(message)
        // Assert
        val reply = answer.reply()
        assertThat(reply.has("error")).isFalse()
        assertThat(reply["result"]["isError"].asBoolean()).isTrue()
        assertThat(reply["result"]["content"][0]["text"].asText()).isEqualTo("The id of the product is required.")
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(
        "a tool that does not exist, '{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"tools/call\",\"params\":{\"name\":\"drop\"}}', -32602",
        "a method that does not exist, '{\"jsonrpc\":\"2.0\",\"id\":6,\"method\":\"resources/list\"}', -32601",
        "a message that is not a request, '{\"jsonrpc\":\"2.0\",\"id\":8}', -32600",
        "a message that is not JSON, 'tools/list', -32700"
    )
    fun `a message the server cannot serve should be answered with an error of the protocol`(
        name: String,
        message: String,
        code: Int
    ) {
        // Act
        val answer = mcpServer.answer(message)
        // Assert
        assertThat(answer.reply()["error"]["code"].asInt()).isEqualTo(code)
    }
}
