package org.flexstore.domain.product

// The products of the catalog, in the order of the catalog
data class Products(private val products: List<Product>) {

    constructor(vararg products: Product) : this(products.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<Product> = products.toList()
}

// The ids of some products of the catalog
data class ProductIds(private val productIds: List<ProductId>) {

    constructor(vararg productIds: ProductId) : this(productIds.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<ProductId> = productIds.toList()
}
