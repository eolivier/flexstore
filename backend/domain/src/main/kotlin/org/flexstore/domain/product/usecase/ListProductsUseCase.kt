package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.verifyCanRead
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
        reader.verifyCanRead("the catalog", ::ProductReadingForbiddenException)
    }
}
