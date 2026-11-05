package org.flexstore.mcp

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

// The versions of the protocol the server agrees on, the latest first
private const val LATEST_PROTOCOL_VERSION = "2025-06-18"
private const val PROTOCOL_VERSION_OF_MARCH_2025 = "2025-03-26"
private const val PROTOCOL_VERSION_OF_NOVEMBER_2024 = "2024-11-05"
private val SUPPORTED_PROTOCOL_VERSIONS =
    listOf(LATEST_PROTOCOL_VERSION, PROTOCOL_VERSION_OF_MARCH_2025, PROTOCOL_VERSION_OF_NOVEMBER_2024)

private const val SERVER_NAME = "flexstore"
private const val SERVER_VERSION = "1.0.0"

private const val JSON_RPC_VERSION = "2.0"

// The methods of the protocol the server serves
private const val INITIALIZE = "initialize"
private const val PING = "ping"
private const val LIST_TOOLS = "tools/list"
private const val CALL_TOOL = "tools/call"

// The codes of the errors of JSON-RPC
private const val PARSE_ERROR = -32700
private const val INVALID_REQUEST = -32600
private const val METHOD_NOT_FOUND = -32601
private const val INVALID_PARAMS = -32602

// A server of the Model Context Protocol, in the way of its Streamable HTTP transport that answers each message
// on its own: a request gets its JSON-RPC answer, a notification is only accepted. It keeps no session
class McpServer(private val productTools: ProductTools) {
    private val json = ObjectMapper()

    fun answer(message: String): McpAnswer {
        try {
            return answer(json.readTree(message))
        } catch (unreadable: JsonProcessingException) {
            return reply(null, errorOf(PARSE_ERROR, "The message is not JSON."))
        }
    }

    private fun answer(message: JsonNode): McpAnswer {
        val id = message.get("id")
        val method = message.path("method").asText()
        if (message.isObject.not() || method.isEmpty()) {
            return if (message.isObject && message.has("result")) McpAnswer.Accepted else invalidRequest(id)
        } else if (id == null) {
            return McpAnswer.Accepted
        } else {
            return reply(id, resultOrError(method, message.path("params")))
        }
    }

    private fun resultOrError(method: String, params: JsonNode): ObjectNode =
        when (method) {
            INITIALIZE -> resultOf(initialization(params))
            PING -> resultOf(json.createObjectNode())
            LIST_TOOLS -> resultOf(toolList())
            CALL_TOOL -> toolCall(params)
            else -> errorOf(METHOD_NOT_FOUND, "The method $method is not supported.")
        }

    private fun initialization(params: JsonNode): ObjectNode {
        val askedVersion = params.path("protocolVersion").asText()
        val initialization = json.createObjectNode()
        initialization.put(
            "protocolVersion",
            if (askedVersion in SUPPORTED_PROTOCOL_VERSIONS) askedVersion else LATEST_PROTOCOL_VERSION
        )
        initialization.putObject("capabilities").putObject("tools").put("listChanged", false)
        initialization.putObject("serverInfo").put("name", SERVER_NAME).put("version", SERVER_VERSION)
        return initialization
    }

    private fun toolList(): ObjectNode {
        val toolList = json.createObjectNode()
        toolList.set<ObjectNode>("tools", productTools.descriptions())
        return toolList
    }

    private fun toolCall(params: JsonNode): ObjectNode {
        val toolName = params.path("name").asText()
        if (productTools.offers(toolName)) {
            return resultOf(content(productTools.call(toolName, params.path("arguments"))))
        } else {
            return errorOf(INVALID_PARAMS, "The tool $toolName does not exist.")
        }
    }

    private fun content(outcome: ToolOutcome): ObjectNode {
        val content = json.createObjectNode()
        val text = when (outcome) {
            is ToolOutcome.Answered -> outcome.text
            is ToolOutcome.Refused -> outcome.reason
        }
        content.putArray("content").addObject().put("type", "text").put("text", text)
        content.put("isError", outcome is ToolOutcome.Refused)
        return content
    }

    private fun invalidRequest(id: JsonNode?) = reply(id, errorOf(INVALID_REQUEST, "The message is not a request."))

    private fun reply(id: JsonNode?, resultOrError: ObjectNode): McpAnswer {
        val reply = json.createObjectNode()
        reply.put("jsonrpc", JSON_RPC_VERSION)
        reply.set<JsonNode>("id", id ?: json.nullNode())
        reply.setAll<ObjectNode>(resultOrError)
        return McpAnswer.Reply(json.writeValueAsString(reply))
    }

    private fun resultOf(result: ObjectNode): ObjectNode {
        val resultOrError = json.createObjectNode()
        resultOrError.set<ObjectNode>("result", result)
        return resultOrError
    }

    private fun errorOf(code: Int, text: String): ObjectNode {
        val resultOrError = json.createObjectNode()
        resultOrError.putObject("error").put("code", code).put("message", text)
        return resultOrError
    }
}

// What the server gives back for a message: the JSON-RPC reply of a request, nothing for a notification
sealed interface McpAnswer {
    data class Reply(val json: String) : McpAnswer
    data object Accepted : McpAnswer
}
