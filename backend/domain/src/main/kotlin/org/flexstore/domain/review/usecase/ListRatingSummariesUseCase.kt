package org.flexstore.domain.review.usecase

import org.flexstore.domain.actor.cannotReadPublicContent
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.review.*
import org.ucop.domain.entity.*

// What the customers think of each product that has reviews: for the catalog
class ListRatingSummariesUseCase(
    private val reviewRepository: ReviewRepository
) : QueryUseCase<Actor, RatingSummaries> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() =
        NominalQuery<Actor, RatingSummaries> { reviewRepository.findAll().summaries() }

    override fun getPostConditions(): PostConditions<RatingSummaries> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        if (reader.cannotReadPublicContent()) {
            throw ReviewReadingForbiddenException(ErrorMessage("Only a visitor, a user or the system can read the ratings of the products."))
        }
    }
}
