package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.ucop.domain.entity.*

// The discount codes given by the administrators, newest first
class ListDiscountCodesUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : QueryUseCase<Unit, DiscountCodes> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, DiscountCodes> { discountCodeRepository.findAll() }

    override fun getPostConditions(): PostConditions<DiscountCodes> = PostConditions.none()
}
