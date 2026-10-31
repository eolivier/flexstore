package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A discount code entered by a customer, usable at that moment: it exists, has not expired, and is not used up
class ReadUsableDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : QueryUseCase<DiscountCodeLookup, DiscountCode> {

    override fun getPreConditions(): PreConditions<DiscountCodeLookup> =
        PreConditions(codeExistsCondition(), codeIsNotExpiredCondition(), codeIsNotUsedUpCondition())

    override fun getNominalQuery() = NominalQuery<DiscountCodeLookup, DiscountCode> { lookup ->
        discountCodeOf(lookup)
    }

    override fun getPostConditions(): PostConditions<DiscountCode> = PostConditions.none()

    // Only called once the code is known
    private fun discountCodeOf(lookup: DiscountCodeLookup) = discountCodeRepository.findBy(lookup.code)!!

    private fun codeExistsCondition() = PreCondition<DiscountCodeLookup> { lookup ->
        if (discountCodeRepository.codeIsMissing(lookup.code)) {
            throw DiscountCodeNotFoundException(ErrorMessage("No discount code ${lookup.code.value}."))
        }
    }

    private fun codeIsNotExpiredCondition() = PreCondition<DiscountCodeLookup> { lookup ->
        if (discountCodeOf(lookup).isExpiredAt(lookup.at)) {
            throw DiscountCodeExpiredException(ErrorMessage("The discount code ${lookup.code.value} has expired."))
        }
    }

    private fun codeIsNotUsedUpCondition() = PreCondition<DiscountCodeLookup> { lookup ->
        if (discountCodeOf(lookup).isUsedUp()) {
            throw DiscountCodeUsedUpException(ErrorMessage("The discount code ${lookup.code.value} is used up."))
        }
    }
}
