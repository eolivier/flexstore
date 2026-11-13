package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.model.ProductId
import org.ucop.domain.entity.Actor

// The use cases of the images of the products
class ProductImageUseCases(
    private val addProductImageUseCase: AddProductImageUseCase,
    private val removeProductImageUseCase: RemoveProductImageUseCase,
    private val readProductImageUseCase: ReadProductImageUseCase,
    private val listProductsWithImageUseCase: ListProductsWithImageUseCase
) {
    fun add(editor: Actor, uploadedImage: UploadedImage) =
        addProductImageUseCase.unfold(ProductImageUpload(editor, uploadedImage))

    fun remove(editor: Actor, productId: ProductId) =
        removeProductImageUseCase.unfold(ProductImageRemoval(editor, productId))

    fun getImage(reader: Actor, productId: ProductId, format: ImageFormat) =
        readProductImageUseCase.unfold(ProductImageLookup(reader, productId, format))

    fun getProductsWithImage(reader: Actor) = listProductsWithImageUseCase.unfold(reader)
}
