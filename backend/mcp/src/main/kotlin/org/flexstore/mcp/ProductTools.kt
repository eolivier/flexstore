package org.flexstore.mcp

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private val prettyJson = Json { prettyPrint = true }

// The tools that expose the products of the shop to an MCP client.
// Each one answers with a text: the products in JSON, or the reason why it cannot give them
class ProductTools(private val productCatalog: ProductCatalog) {
    fun listProducts(arguments: JsonObject): CallToolResult {
        val code = arguments.text("language", Language.byDefault().code)
        if (Language.isSupported(code)) {
            return answering { prettyJson.encodeToString(productCatalog.products(Language.of(code))) }
        } else {
            return unsupported(code)
        }
    }

    fun getProduct(arguments: JsonObject): CallToolResult {
        val code = arguments.text("language", Language.byDefault().code)
        val id = arguments.text("id")
        if (id.isBlank()) {
            return failure("The id of the product is required.")
        } else if (Language.isSupported(code)) {
            val productId = ProductId(id)
            return answering { prettyJson.encodeToString(productCatalog.product(productId, Language.of(code))) }
        } else {
            return unsupported(code)
        }
    }

    private fun answering(answer: () -> String) =
        try {
            CallToolResult(content = listOf(TextContent(answer())))
        } catch (missing: ProductNotFoundException) {
            failure(missing.message.orEmpty())
        } catch (unavailable: CatalogUnavailableException) {
            failure(unavailable.message.orEmpty())
        }

    private fun unsupported(code: String): CallToolResult {
        val supportedCodes = Language.entries.joinToString(", ") { it.code }
        return failure("The language $code is not supported: use one of $supportedCodes.")
    }

    private fun failure(message: String) = CallToolResult(content = listOf(TextContent(message)), isError = true)

    private fun JsonObject.text(name: String, byDefault: String = ""): String =
        this[name]?.jsonPrimitive?.contentOrNull ?: byDefault
}
