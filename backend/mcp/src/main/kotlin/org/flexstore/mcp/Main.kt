package org.flexstore.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val DEFAULT_API_URL = "http://localhost:8080"
private const val LANGUAGE_DESCRIPTION = "The language of the name and the description: en or fr (default: en)"

fun main() {
    val apiUrl = System.getenv("FLEXSTORE_API_URL") ?: DEFAULT_API_URL
    val productTools = ProductTools(FlexstoreProductCatalog(apiUrl))
    val server = flexstoreServer(productTools)
    val transport = StdioServerTransport(System.`in`.asSource().buffered(), System.out.asSink().buffered())
    runBlocking {
        val session = server.createSession(transport)
        val closed = Job()
        session.onClose { closed.complete() }
        closed.join()
    }
}

fun flexstoreServer(productTools: ProductTools): Server {
    val capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))
    val server = Server(Implementation("flexstore", "1.0.0"), ServerOptions(capabilities))
    server.addTool(
        name = "list_products",
        description = "Lists the products of the Flexstore shop: id, SKU, name, description, category, " +
            "price, currency, promotion and current price",
        inputSchema = ToolSchema(properties = languageProperties())
    ) { request -> productTools.listProducts(request.arguments ?: JsonObject(emptyMap())) }
    server.addTool(
        name = "get_product",
        description = "Gives a product of the Flexstore shop from its id",
        inputSchema = ToolSchema(properties = idAndLanguageProperties(), required = listOf("id"))
    ) { request -> productTools.getProduct(request.arguments ?: JsonObject(emptyMap())) }
    return server
}

private fun languageProperties() = buildJsonObject {
    put("language", languageProperty())
}

private fun idAndLanguageProperties() = buildJsonObject {
    put("id", idProperty())
    put("language", languageProperty())
}

private fun idProperty() = buildJsonObject {
    put("type", "string")
    put("description", "The id of the product, as given by list_products")
}

private fun languageProperty() = buildJsonObject {
    put("type", "string")
    put("description", LANGUAGE_DESCRIPTION)
}
