package org.flexstore.domain.image

import org.flexstore.domain.product.model.ProductId

interface ProductImageRepository {

    // Replaces the image of the product in this format
    fun save(productImage: ProductImage)

    fun find(productId: ProductId, format: ImageFormat): ProductImage?

    // The image, which a precondition has checked to exist
    fun get(productId: ProductId, format: ImageFormat): ProductImage =
        find(productId, format) ?: throw ProductImageNotFoundException("Product $productId has no $format image.")

    fun imageIsMissing(productId: ProductId, format: ImageFormat) = find(productId, format) == null

    // The products that have an image
    fun productsWithImage(): List<ProductId>

    fun deleteAllOf(productId: ProductId)
}
