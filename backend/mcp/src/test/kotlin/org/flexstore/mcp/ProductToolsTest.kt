package org.flexstore.mcp

import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertDoesNotThrow

private class InMemoryProductCatalog(private val productsByLanguage: Map<Language, Products>) : ProductCatalog {
    override fun products(language: Language) = productsByLanguage.getValue(language)

    override fun product(productId: ProductId, language: Language): Product =
        productsByLanguage.getValue(language).asList().firstOrNull { it.id == productId.value }
            ?: throw ProductNotFoundException(productId)
}

private class UnreachableProductCatalog : ProductCatalog {
    override fun products(language: Language): Products = throw CatalogUnavailableException("The shop is down")

    override fun product(productId: ProductId, language: Language): Product =
        throw CatalogUnavailableException("The shop is down")
}

class ProductToolsTest {
    private val productCatalog = InMemoryProductCatalog(
        mapOf(
            Language.EN to Products(deskLamp, deskLampOnPromotion.copy(id = "reading-lamp")),
            Language.FR to Products(lampeDeBureau)
        )
    )
    private val productTools = ProductTools(productCatalog)

    private fun CallToolResult.text() = content.filterIsInstance<TextContent>().joinToString("") { it.text }

    private fun arguments(vararg pairs: Pair<String, String>): JsonObject =
        buildJsonObject { pairs.forEach { (name, value) -> put(name, value) } }

    @Test
    fun `listing the products should give them all, in English by default`() {
        // Act
        val answer = productTools.listProducts(arguments())
        // Assert
        val listedProducts = Json.decodeFromString<Products>(answer.text())
        assertFalse(answer.isError == true)
        assertEquals(listOf("desk-lamp", "reading-lamp"), listedProducts.asList().map { it.id })
    }

    @Test
    fun `listing the products in French should give the French names`() {
        // Act
        val answer = productTools.listProducts(arguments("language" to "fr"))
        // Assert
        val listedProducts = Json.decodeFromString<Products>(answer.text())
        assertEquals(listOf("Lampe de bureau"), listedProducts.asList().map { it.name })
    }

    @Test
    fun `getting a product should give it with its promotion`() {
        // Act
        val answer = productTools.getProduct(arguments("id" to "reading-lamp"))
        // Assert
        val product = Json.decodeFromString<Product>(answer.text())
        assertEquals(20.0, product.promotionPercentage)
        assertEquals(28.0, product.currentPrice)
    }

    @Test
    fun `getting a product that does not exist should fail with the reason`() {
        // Act
        val answer = productTools.getProduct(arguments("id" to "unknown"))
        // Assert
        assertTrue(answer.isError == true)
        assertEquals("Product with ID unknown does not exist.", answer.text())
    }

    @Test
    fun `getting a product without its id should fail with the reason`() {
        // Act
        val answer = productTools.getProduct(arguments("language" to "fr"))
        // Assert
        assertTrue(answer.isError == true)
        assertEquals("The id of the product is required.", answer.text())
    }

    @Test
    fun `asking for a language the shop does not have should fail with the languages it has`() {
        // Act
        val answer = productTools.listProducts(arguments("language" to "de"))
        // Assert
        assertTrue(answer.isError == true)
        assertEquals("The language de is not supported: use one of en, fr.", answer.text())
    }

    @Test
    fun `a shop that cannot be reached should be reported, not thrown`() {
        // Arrange
        val productTools = ProductTools(UnreachableProductCatalog())
        // Act
        val answer = assertDoesNotThrow { productTools.listProducts(arguments()) }
        // Assert
        assertTrue(answer.isError == true)
        assertEquals("The shop is down", answer.text())
    }
}
