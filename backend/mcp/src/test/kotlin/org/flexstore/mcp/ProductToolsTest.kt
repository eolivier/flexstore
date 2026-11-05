package org.flexstore.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ProductToolsTest {
    private val json = ObjectMapper()
    private val productTools = ProductTools(productUseCasesOf(deskLamp, readingLamp))

    private fun arguments(text: String): JsonNode = json.readTree(text)

    private fun ToolOutcome.answer(): JsonNode = json.readTree((this as ToolOutcome.Answered).text)

    @Test
    fun `listing the products should give them in English by default`() {
        // Act
        val outcome = productTools.call("list_products", arguments("{}"))
        // Assert
        val listedProducts = outcome.answer()
        assertThat(listedProducts.map { it["id"].asText() }).containsExactly("desk-lamp", "reading-lamp")
        assertThat(listedProducts.map { it["name"].asText() }).containsExactly("Desk lamp", "Reading lamp")
    }

    @Test
    fun `listing the products in French should give the French texts, and English for what is not translated`() {
        // Act
        val outcome = productTools.call("list_products", arguments("""{"language": "fr"}"""))
        // Assert
        val listedProducts = outcome.answer()
        assertThat(listedProducts.map { it["name"].asText() }).containsExactly("Lampe de bureau", "Reading lamp")
    }

    @Test
    fun `a product should give its price, without promotion percentage when it has no promotion`() {
        // Act
        val outcome = productTools.call("get_product", arguments("""{"id": "desk-lamp"}"""))
        // Assert
        val product = outcome.answer()
        assertThat(product["sku"].asText()).isEqualTo("HOM-1")
        assertThat(product["category"].asText()).isEqualTo("HOME")
        assertThat(product["price"].decimalValue()).isEqualByComparingTo("35")
        assertThat(product["currentPrice"].decimalValue()).isEqualByComparingTo("35")
        assertThat(product.has("promotionPercentage")).isFalse()
    }

    @Test
    fun `a product on promotion should give the percentage and the price to pay`() {
        // Act
        val outcome = productTools.call("get_product", arguments("""{"id": "reading-lamp"}"""))
        // Assert
        val product = outcome.answer()
        assertThat(product["promotionPercentage"].decimalValue()).isEqualByComparingTo("20")
        assertThat(product["currentPrice"].decimalValue()).isEqualByComparingTo("28")
    }

    @Test
    fun `a product the domain does not know should be refused with the reason of the domain`() {
        // Act
        val outcome = productTools.call("get_product", arguments("""{"id": "unknown"}"""))
        // Assert
        assertThat(outcome).isEqualTo(ToolOutcome.Refused("Product with ID unknown does not exist."))
    }

    @ParameterizedTest(name = "the arguments {0} are refused")
    @ValueSource(strings = ["{}", """{"id": ""}""", """{"id": "  "}"""])
    fun `a product asked without its id should be refused`(text: String) {
        // Act
        val outcome = productTools.call("get_product", arguments(text))
        // Assert
        assertThat(outcome).isEqualTo(ToolOutcome.Refused("The id of the product is required."))
    }

    @Test
    fun `a language the shop does not have should be refused with the languages it has`() {
        // Act
        val outcome = productTools.call("list_products", arguments("""{"language": "de"}"""))
        // Assert
        assertThat(outcome).isEqualTo(ToolOutcome.Refused("The language de is not supported: use one of EN, FR."))
    }

    @Test
    fun `the tools should be described with their arguments`() {
        // Act
        val descriptions = productTools.descriptions()
        // Assert
        assertThat(descriptions.map { it["name"].asText() }).containsExactly("list_products", "get_product")
        val getProduct = descriptions[1]["inputSchema"]
        assertThat(getProduct["required"].map { it.asText() }).containsExactly("id")
        assertThat(getProduct["properties"].fieldNames().asSequence().toList()).containsExactly("language", "id")
    }
}
