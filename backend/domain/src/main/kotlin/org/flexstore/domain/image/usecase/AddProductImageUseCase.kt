package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives the image of a product (2 MB at most): it is kept resized in each format,
// replacing the previous one
class AddProductImageUseCase(
    private val productImageRepository: ProductImageRepository,
    private val productRepository: ProductRepository,
    private val imageResizerRepository: ImageResizerRepository
) : UseCase<UploadedImage> {

    override fun getPreConditions(): PreConditions<UploadedImage> =
        PreConditions(productExistsCondition(), imageIsNotTooLargeCondition())

    override fun getNominalScenario(): NominalScenario<UploadedImage> {
        val resizeStep = Step<UploadedImage> { uploadedImage ->
            ImageFormat.entries.forEach { format ->
                val resizedContent = imageResizerRepository.resize(uploadedImage.content, format)
                productImageRepository.save(ProductImage(uploadedImage.productId, format, resizedContent))
            }
        }
        return NominalScenario(resizeStep)
    }

    override fun getPostConditions(): PostConditions<UploadedImage> =
        PostConditions(eachFormatIsStoredWithinItsSizeCondition())

    private fun productExistsCondition() = PreCondition<UploadedImage> { uploadedImage ->
        if (productRepository.productIsMissing(uploadedImage.productId)) {
            val message = ErrorMessage("Product with ID ${uploadedImage.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun imageIsNotTooLargeCondition() = PreCondition<UploadedImage> { uploadedImage ->
        if (uploadedImage.isTooLarge()) {
            val message = ErrorMessage("An image is ${UploadedImage.MAX_BYTES / 1024 / 1024} MB at most.")
            throw ImageTooLargeException(message)
        }
    }

    private fun eachFormatIsStoredWithinItsSizeCondition() = PostCondition<UploadedImage> { uploadedImage ->
        ImageFormat.entries.forEach { format ->
            val productImage = productImageRepository.find(uploadedImage.productId, format)
            if (productImage == null || !format.accepts(productImage.content)) {
                val message = ErrorMessage("The $format image of ${uploadedImage.productId.id.value} was not kept.")
                throw ProductImageStorageFailed(message)
            }
        }
    }
}
