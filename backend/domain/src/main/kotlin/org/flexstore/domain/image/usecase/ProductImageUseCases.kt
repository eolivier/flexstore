package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.user.model.UserId

// The use cases of the images of the products
class ProductImageUseCases(
    private val addProductImageUseCase: AddProductImageUseCase,
    private val removeProductImageUseCase: RemoveProductImageUseCase,
    private val readProductImageUseCase: ReadProductImageUseCase,
    private val listProductsWithImageUseCase: ListProductsWithImageUseCase
) {
    fun add(editor: UserId, uploadedImage: UploadedImage) =
        addProductImageUseCase.unfold(ProductImageUpload(editor, uploadedImage))

    fun remove(editor: UserId, productId: ProductId) =
        removeProductImageUseCase.unfold(ProductImageRemoval(editor, productId))

    fun getImage(productId: ProductId, format: ImageFormat) =
        readProductImageUseCase.unfold(ProductImageLookup(productId, format))

    fun getProductsWithImage() = listProductsWithImageUseCase.unfold(Unit)
}
