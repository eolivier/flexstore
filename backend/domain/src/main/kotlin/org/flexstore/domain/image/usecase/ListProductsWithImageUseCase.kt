package org.flexstore.domain.image.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.image.*
import org.flexstore.domain.product.model.ProductIds
import org.ucop.domain.entity.*

// The products that have an image
class ListProductsWithImageUseCase(
    private val productImageRepository: ProductImageRepository
) : QueryUseCase<Actor, ProductIds> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<Actor, ProductIds> {
        ProductIds(productImageRepository.productsWithImage())
    }

    override fun getPostConditions(): PostConditions<ProductIds> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        if (reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("The reader must be at least a visitor to read the products that have an image.")
            throw ProductImageReadingForbiddenException(message)
        }
    }
}
