package org.flexstore.domain.review.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.review.*
import org.ucop.domain.entity.*

// The reviews of a product, newest first
// A reader asks for the reviews of a product
data class ProductReviewsReading(val reader: Actor, val productId: ProductId)

class ListProductReviewsUseCase(
    private val reviewRepository: ReviewRepository
) : QueryUseCase<ProductReviewsReading, Reviews> {

    override fun getPreConditions(): PreConditions<ProductReviewsReading> =
        PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<ProductReviewsReading, Reviews> { reading ->
        reviewRepository.findAllAbout(reading.productId).newestFirst()
    }

    override fun getPostConditions(): PostConditions<Reviews> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<ProductReviewsReading> { reading ->
        if (reading.reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("Only a visitor, a user or the system can read the reviews.")
            throw ReviewReadingForbiddenException(message)
        }
    }
}
