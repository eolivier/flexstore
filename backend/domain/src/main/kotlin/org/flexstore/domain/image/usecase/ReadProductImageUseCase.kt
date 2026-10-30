package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// The image of a product looked for, in one of its formats
data class ProductImageLookup(val productId: ProductId, val format: ImageFormat)

// The image of a product, in the format asked
class ReadProductImageUseCase(
    private val productImageRepository: ProductImageRepository
) : QueryUseCase<ProductImageLookup, ProductImage> {

    override fun getPreConditions(): PreConditions<ProductImageLookup> = PreConditions(imageExistsCondition())

    override fun getNominalQuery() = NominalQuery<ProductImageLookup, ProductImage> { lookup ->
        productImageRepository.find(lookup.productId, lookup.format)!!
    }

    override fun getPostConditions(): PostConditions<ProductImage> = PostConditions.none()

    private fun imageExistsCondition() = PreCondition<ProductImageLookup> { lookup ->
        if (productImageRepository.imageIsMissing(lookup.productId, lookup.format)) {
            throw ProductImageNotFoundException(ErrorMessage("Product ${lookup.productId.id.value} has no image."))
        }
    }
}
