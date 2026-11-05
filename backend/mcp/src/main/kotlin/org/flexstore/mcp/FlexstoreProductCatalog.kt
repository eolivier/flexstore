package org.flexstore.mcp

import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private const val OK = 200
private const val NOT_FOUND = 404

// The catalog of the shop, read from the public products of its REST API (openapi.yaml)
class FlexstoreProductCatalog(
    private val apiUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) : ProductCatalog {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override fun products(language: Language): Products {
        val response = get("/api/products/", language)
        if (response.statusCode() == OK) {
            return parse<Products>(response)
        } else {
            throw unexpected(response)
        }
    }

    override fun product(productId: ProductId, language: Language): Product {
        // URLEncoder writes a space as a plus, which is right in a form but a plus in a path
        val encodedId = URLEncoder.encode(productId.value, StandardCharsets.UTF_8).replace("+", "%20")
        val response = get("/api/products/$encodedId", language)
        return when (response.statusCode()) {
            OK -> parse<Product>(response)
            NOT_FOUND -> throw ProductNotFoundException(productId)
            else -> throw unexpected(response)
        }
    }

    private fun get(path: String, language: Language): HttpResponse<String> {
        val uri = URI.create(apiUrl.trimEnd('/') + path)
        val request = HttpRequest.newBuilder(uri)
            .header("Accept", "application/json")
            .header("Accept-Language", language.code)
            .GET()
            .build()
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (failure: IOException) {
            throw CatalogUnavailableException("The shop at $apiUrl cannot be reached: ${failure.message}")
        }
    }

    private inline fun <reified Body> parse(response: HttpResponse<String>): Body {
        try {
            return json.decodeFromString(response.body())
        } catch (failure: SerializationException) {
            throw CatalogUnavailableException("The shop gave an unreadable answer: ${failure.message}")
        }
    }

    private fun unexpected(response: HttpResponse<String>) =
        CatalogUnavailableException("The shop answered with the status ${response.statusCode()}.")
}
