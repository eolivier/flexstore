package org.flexstore.domain.image

import org.flexstore.domain.product.ProductId

interface ProductImageRepository {

    // Replaces the image of the product in this format
    fun save(productImage: ProductImage)

    fun find(productId: ProductId, format: ImageFormat): ProductImage?

    fun imageIsMissing(productId: ProductId, format: ImageFormat) = find(productId, format) == null

    // The products that have an image
    fun productsWithImage(): List<ProductId>

    fun deleteAllOf(productId: ProductId)
}
