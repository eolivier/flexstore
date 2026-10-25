package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId

// The use cases of the images of the products
class ProductImageUseCases(
    private val addProductImageUseCase: AddProductImageUseCase,
    private val removeProductImageUseCase: RemoveProductImageUseCase,
    private val readProductImageUseCase: ReadProductImageUseCase
) {
    fun add(uploadedImage: UploadedImage) = addProductImageUseCase.unfold(uploadedImage)

    fun remove(productId: ProductId) = removeProductImageUseCase.unfold(productId)

    fun getImage(productId: ProductId, format: ImageFormat) = readProductImageUseCase.getImage(productId, format)

    fun getProductsWithImage() = readProductImageUseCase.getProductsWithImage()
}
