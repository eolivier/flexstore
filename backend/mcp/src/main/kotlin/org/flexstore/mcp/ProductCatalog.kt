package org.flexstore.mcp

// What the tools know about the catalog of the shop
interface ProductCatalog {
    fun products(language: Language): Products

    // Fails with ProductNotFoundException when the shop has no such product
    fun product(productId: ProductId, language: Language): Product
}

class ProductNotFoundException(productId: ProductId) :
    RuntimeException("Product with ID ${productId.value} does not exist.")

class CatalogUnavailableException(reason: String) : RuntimeException(reason)
