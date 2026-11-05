package org.flexstore.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.product.model.PercentageOffPromotion
import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.product.model.ProductId
import org.ucop.domain.NominalException

private const val LIST_PRODUCTS = "list_products"
private const val GET_PRODUCT = "get_product"

// The tools that expose the products of the catalog to an MCP client. They call the use cases of the domain: the
// answer of a tool is a text, the product in JSON, or the reason why the domain refuses to give it
class ProductTools(private val productUseCases: ProductUseCases) {
    private val json = ObjectMapper()

    fun descriptions(): ArrayNode {
        val descriptions = json.createArrayNode()
        descriptions.add(listProductsDescription())
        descriptions.add(getProductDescription())
        return descriptions
    }

    fun offers(toolName: String) = toolName == LIST_PRODUCTS || toolName == GET_PRODUCT

    fun call(toolName: String, arguments: JsonNode): ToolOutcome {
        val languageCode = arguments.path("language").asText(Language.DEFAULT.name)
        val languages = Language.entries.filter { it.name.equals(languageCode, ignoreCase = true) }
        if (languages.isEmpty()) {
            return ToolOutcome.Refused("The language $languageCode is not supported: use one of ${languageCodes()}.")
        } else if (toolName == LIST_PRODUCTS) {
            return answering { listProducts(languages.single()) }
        } else {
            return answering { getProduct(arguments.path("id").asText(), languages.single()) }
        }
    }

    private fun listProducts(language: Language): String {
        val productsJson = json.createArrayNode()
        productUseCases.getProducts().asList().forEach { product ->
            productsJson.add(productJson(product as DefinedProduct, language))
        }
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(productsJson)
    }

    private fun getProduct(id: String, language: Language): String {
        if (id.isBlank()) {
            throw MissingArgumentException("The id of the product is required.")
        }
        val product = productUseCases.getProduct(ProductId(Identity(id)))
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

    private fun listProductsDescription(): ObjectNode {
        val description = toolDescription(
            LIST_PRODUCTS,
            "Lists the products of the Flexstore shop: id, SKU, name, description, category, price, currency, " +
                "promotion percentage (while a promotion is running) and current price"
        )
        inputSchemaOf(description).set<ObjectNode>("properties", languageProperty())
        return description
    }

    private fun getProductDescription(): ObjectNode {
        val description = toolDescription(GET_PRODUCT, "Gives a product of the Flexstore shop from its id")
        val inputSchema = inputSchemaOf(description)
        val properties = languageProperty()
        properties.set<ObjectNode>("id", textProperty("The id of the product, as given by $LIST_PRODUCTS"))
        inputSchema.set<ObjectNode>("properties", properties)
        inputSchema.putArray("required").add("id")
        return description
    }

    private fun toolDescription(name: String, text: String): ObjectNode {
        val description = json.createObjectNode()
        description.put("name", name)
        description.put("description", text)
        description.putObject("inputSchema").put("type", "object")
        return description
    }

    private fun inputSchemaOf(description: ObjectNode) = description.get("inputSchema") as ObjectNode

    private fun languageProperty(): ObjectNode {
        val properties = json.createObjectNode()
        val text = "The language of the name and the description: ${languageCodes()} (default: ${Language.DEFAULT.name})"
        properties.set<ObjectNode>("language", textProperty(text))
        return properties
    }

    private fun textProperty(description: String): ObjectNode {
        val property = json.createObjectNode()
        property.put("type", "string")
        property.put("description", description)
        return property
    }

    private fun languageCodes() = Language.entries.joinToString(", ") { it.name }
}

private class MissingArgumentException(message: String) : RuntimeException(message)

// What a tool gives back: the answer, or the reason why it refuses to answer (an error the client reads)
sealed interface ToolOutcome {
    data class Answered(val text: String) : ToolOutcome
    data class Refused(val reason: String) : ToolOutcome
}
