package org.flexstore.mcp

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Products(private val products: List<Product>) {
    constructor(vararg products: Product) : this(products.toList())

    fun asList() = products
}
