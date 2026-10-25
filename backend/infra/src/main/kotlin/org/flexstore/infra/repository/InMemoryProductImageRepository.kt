package org.flexstore.infra.repository

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId

class InMemoryProductImageRepository : ProductImageRepository {

    private val images = mutableMapOf<Pair<ProductId, ImageFormat>, ProductImage>()

    override fun save(productImage: ProductImage) {
        images[productImage.productId to productImage.format] = productImage
    }

    override fun find(productId: ProductId, format: ImageFormat) = images[productId to format]

    override fun productsWithImage() = images.keys.map { it.first }.distinct()

    override fun deleteAllOf(productId: ProductId) {
        images.keys.removeIf { it.first == productId }
    }
}
