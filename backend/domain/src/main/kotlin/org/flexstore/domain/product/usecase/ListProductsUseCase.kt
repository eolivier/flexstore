package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.ucop.domain.entity.*

// The products of the catalog
class ListProductsUseCase(private val productRepository: ProductRepository) : QueryUseCase<Actor, Products> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<Actor, Products> { Products(productRepository.findAll()) }

    override fun getPostConditions(): PostConditions<Products> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        if (reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("The reader must be at least a visitor to read the catalog.")
            throw ProductReadingForbiddenException(message)
        }
    }
}
