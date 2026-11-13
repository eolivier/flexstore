package org.flexstore.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification
import io.modelcontextprotocol.spec.McpSchema.CallToolResult
import io.modelcontextprotocol.spec.McpSchema.JsonSchema
import io.modelcontextprotocol.spec.McpSchema.Tool
import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.product.model.PercentageOffPromotion
import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.product.model.ProductId
import org.ucop.domain.NominalException
import org.ucop.domain.entity.Actor

private const val LIST_PRODUCTS = "list_products"
private const val GET_PRODUCT = "get_product"
private const val LANGUAGE = "language"
private const val ID = "id"

// The tools that expose the products of the catalog to an MCP client, in the types of the MCP Java SDK that
// serves the protocol. They call the use cases of the domain: the answer of a tool is a text, the product in
// JSON, or the reason why the domain refuses to give it. The client is not signed in: the reader is a visitor
class ProductTools(private val productUseCases: ProductUseCases, private val reader: Actor) {
    private val json = ObjectMapper()

    fun specifications(): List<SyncToolSpecification> =
        listOf(listProductsSpecification(), getProductSpecification())

    fun call(toolName: String, arguments: Map<String, Any>): ToolOutcome {
        val languageCode = arguments.getOrDefault(LANGUAGE, Language.DEFAULT.name).toString()
        val languages = Language.entries.filter { it.name.equals(languageCode, ignoreCase = true) }
        if (languages.isEmpty()) {
            return ToolOutcome.Refused(
                "The language $languageCode is not supported: use one of ${languageCodes()}.",
            )
        } else if (toolName == LIST_PRODUCTS) {
            return answering { listProducts(languages.single()) }
        } else {
            return answering { getProduct(arguments.getOrDefault(ID, "").toString(), languages.single()) }
        }
    }

    private fun listProductsSpecification(): SyncToolSpecification {
        val description = "Lists the products of the Flexstore shop: id, SKU, name, description, category, " +
            "price, currency, promotion percentage (while a promotion is running) and current price"
        return specificationOf(LIST_PRODUCTS, description, inputSchemaOf(languageProperty()))
    }

    private fun getProductSpecification(): SyncToolSpecification {
        val properties =
            languageProperty() + (ID to textProperty("The id of the product, as given by $LIST_PRODUCTS"))
        val inputSchema = inputSchemaOf(properties, listOf(ID))
        return specificationOf(GET_PRODUCT, "Gives a product of the Flexstore shop from its id", inputSchema)
    }

    private fun specificationOf(
        name: String,
        description: String,
        inputSchema: JsonSchema
    ): SyncToolSpecification {
        val tool = Tool.builder().name(name).description(description).inputSchema(inputSchema).build()
        return SyncToolSpecification(tool) { _, request -> resultOf(call(name, request.arguments().orEmpty())) }
    }

    private fun resultOf(outcome: ToolOutcome): CallToolResult {
        val result = CallToolResult.builder()
        return when (outcome) {
            is ToolOutcome.Answered -> result.addTextContent(outcome.text).isError(false).build()
            is ToolOutcome.Refused -> result.addTextContent(outcome.reason).isError(true).build()
        }
    }

    private fun listProducts(language: Language): String {
        val productsJson = json.createArrayNode()
        productUseCases.getProducts(reader).asList().forEach { product ->
            productsJson.add(productJson(product as DefinedProduct, language))
        }
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(productsJson)
    }

    private fun getProduct(id: String, language: Language): String {
        if (id.isBlank()) {
            throw MissingArgumentException("The id of the product is required.")
        }
        val product = productUseCases.getProduct(reader, ProductId(Identity(id)))
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(productJson(product, language))
    }

    private fun answering(answer: () -> String) =
        try {
            ToolOutcome.Answered(answer())
        } catch (refused: NominalException) {
            ToolOutcome.Refused(refused.message.orEmpty())
        } catch (missing: MissingArgumentException) {
            ToolOutcome.Refused(missing.message.orEmpty())
        }

    // The name and the description in the language asked, or in the default language when the product is not
    // translated into it
    private fun productJson(product: DefinedProduct, language: Language): ObjectNode {
        val translation = product.translationIn(language)
        val productJson = json.createObjectNode()
        productJson.put("id", product.productId.id.value)
        productJson.put("sku", product.sku.value)
        productJson.put("name", translation.name.value)
        productJson.put("description", translation.description.value)
        productJson.put("category", product.category.name)
        productJson.put("price", product.price.amount.value)
        productJson.put("currency", product.price.currency.symbol)
        val promotion = product.promotion
        if (promotion is PercentageOffPromotion && promotion.isRunning()) {
            productJson.put("promotionPercentage", promotion.percentage.value)
        }
        productJson.put("currentPrice", product.currentPrice.amount.value)
        return productJson
    }

    // The input of a tool is an object, whose properties are given by name; the SDK leaves the other parts
    // of the JSON schema (additional properties, definitions) to its defaults
    private fun inputSchemaOf(properties: Map<String, Any>, required: List<String>) =
        JsonSchema("object", properties, required, null, null, null)

    private fun inputSchemaOf(properties: Map<String, Any>) = inputSchemaOf(properties, emptyList())

    private fun languageProperty(): Map<String, Any> {
        val defaultLanguage = Language.DEFAULT.name
        val text = "The language of the name and the description: ${languageCodes()} (default: $defaultLanguage)"
        return mapOf(LANGUAGE to textProperty(text))
    }

    private fun textProperty(description: String): Map<String, Any> =
        mapOf("type" to "string", "description" to description)

    private fun languageCodes() = Language.entries.joinToString(", ") { it.name }
}

private class MissingArgumentException(message: String) : RuntimeException(message)

// What a tool gives back: the answer, or the reason why it refuses to answer (an error the client reads)
sealed interface ToolOutcome {
    data class Answered(val text: String) : ToolOutcome
    data class Refused(val reason: String) : ToolOutcome
}
