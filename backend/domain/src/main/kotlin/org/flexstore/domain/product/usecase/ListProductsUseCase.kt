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
            throw ProductReadingForbiddenException(ErrorMessage("Only a visitor, a user or the system can read the catalog."))
        }
    }
}
