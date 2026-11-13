package org.flexstore.domain.image.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.flexstore.domain.image.*
import org.flexstore.domain.product.model.ProductId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The image of a product looked for, in one of its formats
data class ProductImageLookup(val reader: Actor, val productId: ProductId, val format: ImageFormat)

// The image of a product, in the format asked
class ReadProductImageUseCase(
    private val productImageRepository: ProductImageRepository
) : QueryUseCase<ProductImageLookup, ProductImage> {

    override fun getPreConditions(): PreConditions<ProductImageLookup> =
        PreConditions(readerCanReadCondition(), imageExistsCondition())

    override fun getNominalQuery() = NominalQuery<ProductImageLookup, ProductImage> { lookup ->
        productImageRepository.find(lookup.productId, lookup.format)!!
    }

    override fun getPostConditions(): PostConditions<ProductImage> = PostConditions.none()

    private fun imageExistsCondition() = PreCondition<ProductImageLookup> { lookup ->
        if (productImageRepository.imageIsMissing(lookup.productId, lookup.format)) {
            val message = ErrorMessage("Product ${lookup.productId} has no image.")
            throw ProductImageNotFoundException(message)
        }
    }

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<ProductImageLookup> { lookup ->
        if (lookup.reader.isNotAtLeastVisitor()) {
            val reason = "The reader must be at least a visitor to read the images of the products."
            val message = ErrorMessage(reason)
            throw ProductImageReadingForbiddenException(message)
        }
    }
}
