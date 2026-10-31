package org.flexstore.domain.review.usecase

import org.flexstore.domain.product.ProductId
import org.flexstore.domain.review.*
import org.ucop.domain.entity.*

// The reviews of a product, newest first
class ListProductReviewsUseCase(
    private val reviewRepository: ReviewRepository
) : QueryUseCase<ProductId, Reviews> {

    override fun getPreConditions(): PreConditions<ProductId> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<ProductId, Reviews> { productId ->
        reviewRepository.findAllAbout(productId).newestFirst()
    }

    override fun getPostConditions(): PostConditions<Reviews> = PostConditions.none()
}
