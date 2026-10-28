package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.ucop.domain.entity.*

// The products that have an image
class ListProductsWithImageUseCase(
    private val productImageRepository: ProductImageRepository
) : QueryUseCase<Unit, List<ProductId>> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, List<ProductId>> {
        productImageRepository.productsWithImage()
    }

    override fun getPostConditions(): PostConditions<List<ProductId>> = PostConditions.none()
}
