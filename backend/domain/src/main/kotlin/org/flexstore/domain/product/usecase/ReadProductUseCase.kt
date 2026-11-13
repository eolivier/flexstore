package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A reader asks for a product of the catalog
data class ProductReading(val reader: Actor, val productId: ProductId)

class ReadProductUseCase(
    private val productRepository: ProductRepository
) : QueryUseCase<ProductReading, DefinedProduct> {

    override fun getPreConditions(): PreConditions<ProductReading> = 
        PreConditions(readerCanReadCondition(), productExistsCondition())

    override fun getNominalQuery() = NominalQuery<ProductReading, DefinedProduct> { reading ->
        productRepository.findById(reading.productId)!!
    }

    override fun getPostConditions(): PostConditions<DefinedProduct> = PostConditions.none()

    private fun productExistsCondition() = PreCondition<ProductReading> { reading ->
        if (productRepository.productIsMissing(reading.productId)) {
            val message = ErrorMessage("Product with ID ${reading.productId} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<ProductReading> { reading ->
        if (reading.reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("The reader must be at least a visitor to read the catalog.")
            throw ProductReadingForbiddenException(message)
        }
    }
}
