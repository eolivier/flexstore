package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.UseCaseLog

class ReadProductImageUseCase(private val productImageRepository: ProductImageRepository) {

    fun getImage(productId: ProductId, format: ImageFormat): ProductImage =
        UseCaseLog.execution("ReadProductImageUseCase.getImage") {
            val notFound = ProductImageNotFoundException(ErrorMessage("Product ${productId.id.value} has no image."))
            productImageRepository.find(productId, format) ?: throw notFound
        }

    fun getProductsWithImage(): List<ProductId> =
        UseCaseLog.execution("ReadProductImageUseCase.getProductsWithImage") {
            productImageRepository.productsWithImage()
        }
}
