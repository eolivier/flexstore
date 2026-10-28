package org.flexstore.domain.product

import org.ucop.domain.entity.*

// The products of the catalog
class ListProductsUseCase(private val productRepository: ProductRepository) : QueryUseCase<Unit, List<Product>> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, List<Product>> { productRepository.findAll() }

    override fun getPostConditions(): PostConditions<List<Product>> = PostConditions.none()
}
