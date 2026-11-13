package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives the image of a product (2 MB at most): it is kept resized in each format,
// replacing the previous one
class AddProductImageUseCase(
    private val productImageRepository: ProductImageRepository,
    private val productRepository: ProductRepository,
    private val imageResizerRepository: ImageResizerRepository
) : UseCase<ProductImageUpload> {

    override fun getPreConditions(): PreConditions<ProductImageUpload> =
        PreConditions(editorIsAdministratorCondition(), productExistsCondition(), imageIsNotTooLargeCondition())

    override fun getNominalScenario(): NominalScenario<ProductImageUpload> {
        val resizeStep = Step<ProductImageUpload> { (_, uploadedImage) ->
            ImageFormat.entries.forEach { format ->
                val resizedContent = imageResizerRepository.resize(uploadedImage.content, format)
                productImageRepository.save(ProductImage(uploadedImage.productId, format, resizedContent))
            }
        }
        return NominalScenario(resizeStep)
    }

    override fun getPostConditions(): PostConditions<ProductImageUpload> =
        PostConditions(eachFormatIsStoredWithinItsSizeCondition())

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<ProductImageUpload> { request ->
        if (request.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can give the image of a product.")
            throw ProductImageForbiddenException(message)
        }
    }

    private fun productExistsCondition() = PreCondition<ProductImageUpload> { (_, uploadedImage) ->
        if (productRepository.productIsMissing(uploadedImage.productId)) {
            val message = ErrorMessage("Product with ID ${uploadedImage.productId.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun imageIsNotTooLargeCondition() = PreCondition<ProductImageUpload> { (_, uploadedImage) ->
        if (uploadedImage.isTooLarge()) {
            val message = ErrorMessage("An image is ${UploadedImage.MAX_MEGABYTES} MB at most.")
            throw ImageTooLargeException(message)
        }
    }

    private fun eachFormatIsStoredWithinItsSizeCondition() = PostCondition<ProductImageUpload> { upload ->
        val uploadedImage = upload.uploadedImage
        ImageFormat.entries.forEach { format ->
            val productImage = productImageRepository.find(uploadedImage.productId, format)
            if (productImage == null || format.refuses(productImage.content)) {
                val reason = "The $format image of ${uploadedImage.productId.value} was not kept."
                val message = ErrorMessage(reason)
                throw ProductImageStorageFailed(message)
            }
        }
    }
}
