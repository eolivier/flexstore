package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import org.flexstore.domain.user.UserRepository

// An administrator gives the image of a product (2 MB at most): it is kept resized in each format,
// replacing the previous one
class AddProductImageUseCase(
    private val productImageRepository: ProductImageRepository,
    private val productRepository: ProductRepository,
    private val imageResizerRepository: ImageResizerRepository,
    private val userRepository: UserRepository
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

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<ProductImageUpload> { request ->
        if (userRepository.isNotAdministrator(request.editor)) {
            throw ProductImageForbiddenException(ErrorMessage("Only an administrator can give the image of a product."))
        }
    }

    private fun productExistsCondition() = PreCondition<ProductImageUpload> { (_, uploadedImage) ->
        if (productRepository.productIsMissing(uploadedImage.productId)) {
            val message = ErrorMessage("Product with ID ${uploadedImage.productId.id.value} does not exist.")
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
            if (productImage == null || !format.accepts(productImage.content)) {
                val message = ErrorMessage("The $format image of ${uploadedImage.productId.id.value} was not kept.")
                throw ProductImageStorageFailed(message)
            }
        }
    }
}
