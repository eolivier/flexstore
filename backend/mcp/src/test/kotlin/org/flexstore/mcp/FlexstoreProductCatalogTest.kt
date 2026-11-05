package org.flexstore.mcp

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

private const val OK = 200
private const val NOT_FOUND = 404

// The REST API of the shop, played by a local server that records what it is asked
class FlexstoreProductCatalogTest {
    private val shop = HttpServer.create(InetSocketAddress("localhost", 0), 0)
    private val askedPaths = mutableListOf<String>()
    private val askedLanguages = mutableListOf<String>()
    private var opened = false

    private val apiUrl get() = "http://localhost:${shop.address.port}"

    private fun answer(status: Int, body: String) {
        shop.createContext("/") { exchange: HttpExchange ->
            askedPaths.add(exchange.requestURI.path)
            askedLanguages.add(exchange.requestHeaders.getFirst("Accept-Language"))
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        shop.start()
        opened = true
    }

    // Stopping a server that was never started waits forever
    @AfterTest
    fun closeShop() {
        if (opened) {
            shop.stop(0)
        }
    }

    @Test
    fun `the products should be read from the API in the language asked`() {
        // Arrange
        answer(OK, """[{"id":"desk-lamp","sku":"HOM-1","name":"Lampe","description":"Une lampe",
            |"category":"Home","price":35,"currency":"EUR","promotionPercentage":null,"currentPrice":35,
            |"unknownField":"ignored"}]""".trimMargin())
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        val products = productCatalog.products(Language.FR)
        // Assert
        assertEquals(listOf("/api/products/"), askedPaths)
        assertEquals(listOf("fr"), askedLanguages)
        assertEquals(listOf("Lampe"), products.asList().map { it.name })
        assertEquals(0.0, products.asList().single().promotionPercentage)
    }

    @Test
    fun `a product should be read from the API with its promotion`() {
        // Arrange
        answer(OK, """{"id":"desk-lamp","sku":"HOM-1","name":"Desk lamp","description":"A lamp",
            |"category":"Home","price":35,"currency":"EUR","promotionPercentage":20,"currentPrice":28}"""
            .trimMargin())
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        val product = productCatalog.product(ProductId("desk-lamp"), Language.EN)
        // Assert
        assertEquals(listOf("/api/products/desk-lamp"), askedPaths)
        assertEquals(20.0, product.promotionPercentage)
        assertEquals(28.0, product.currentPrice)
    }

    @Test
    fun `an id with special characters should be encoded in the path`() {
        // Arrange
        answer(NOT_FOUND, "")
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        assertFailsWith<ProductNotFoundException> { productCatalog.product(ProductId("a b/c"), Language.EN) }
        // Assert
        assertEquals(listOf("/api/products/a b/c"), askedPaths)
    }

    @Test
    fun `a product the shop does not know should be reported as not found`() {
        // Arrange
        answer(NOT_FOUND, "")
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        val thrown = assertFailsWith<ProductNotFoundException> {
            productCatalog.product(ProductId("unknown"), Language.EN)
        }
        // Assert
        assertEquals("Product with ID unknown does not exist.", thrown.message)
    }

    @ParameterizedTest(name = "a status {0} should be reported as unavailable")
    @ValueSource(ints = [401, 500, 503])
    fun `an unexpected status should be reported as unavailable`(status: Int) {
        // Arrange
        answer(status, "")
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        val thrown = assertFailsWith<CatalogUnavailableException> { productCatalog.products(Language.EN) }
        // Assert
        assertEquals("The shop answered with the status $status.", thrown.message)
    }

    @Test
    fun `an answer that is not readable should be reported as unavailable`() {
        // Arrange
        answer(OK, "<html>not the API</html>")
        val productCatalog = FlexstoreProductCatalog(apiUrl)
        // Act
        val thrown = assertFailsWith<CatalogUnavailableException> { productCatalog.products(Language.EN) }
        // Assert
        assertEquals(true, thrown.message.orEmpty().startsWith("The shop gave an unreadable answer"))
    }

    @Test
    fun `a shop that cannot be reached should be reported as unavailable`() {
        // Arrange
        val unusedPort = ServerSocket(0).use { it.localPort }
        val productCatalog = FlexstoreProductCatalog("http://localhost:$unusedPort")
        // Act
        val thrown = assertFailsWith<CatalogUnavailableException> { productCatalog.products(Language.EN) }
        // Assert
        assertEquals(true, thrown.message.orEmpty().startsWith("The shop at http://localhost:$unusedPort"))
    }
}
