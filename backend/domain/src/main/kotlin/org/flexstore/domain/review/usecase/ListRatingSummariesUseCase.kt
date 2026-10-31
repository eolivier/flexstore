package org.flexstore.domain.review.usecase

import org.flexstore.domain.review.*
import org.ucop.domain.entity.*

// What the customers think of each product that has reviews: for the catalog
class ListRatingSummariesUseCase(
    private val reviewRepository: ReviewRepository
) : QueryUseCase<Unit, RatingSummaries> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() =
        NominalQuery<Unit, RatingSummaries> { reviewRepository.findAll().summaries() }

    override fun getPostConditions(): PostConditions<RatingSummaries> = PostConditions.none()
}
