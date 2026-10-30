package org.flexstore.domain.product

import org.ucop.domain.entity.*

// The products of the catalog
class ListProductsUseCase(private val productRepository: ProductRepository) : QueryUseCase<Unit, Products> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, Products> { Products(productRepository.findAll()) }

    override fun getPostConditions(): PostConditions<Products> = PostConditions.none()
}
