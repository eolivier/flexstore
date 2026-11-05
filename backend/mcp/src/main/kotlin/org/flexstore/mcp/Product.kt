package org.flexstore.mcp

import kotlinx.serialization.Serializable

@JvmInline
value class ProductId(val value: String)

// A product of the shop, as the customers see it: its name and description in their language
@Serializable
data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val currency: String,
    // Zero when no promotion is running
    val promotionPercentage: Double = 0.0,
    val currentPrice: Double
)
