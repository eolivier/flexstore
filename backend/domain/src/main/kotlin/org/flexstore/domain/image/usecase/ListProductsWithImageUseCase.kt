package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductIds
import org.ucop.domain.entity.*

// The products that have an image
class ListProductsWithImageUseCase(
    private val productImageRepository: ProductImageRepository
) : QueryUseCase<Unit, ProductIds> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, ProductIds> {
        ProductIds(productImageRepository.productsWithImage())
    }

    override fun getPostConditions(): PostConditions<ProductIds> = PostConditions.none()
}
