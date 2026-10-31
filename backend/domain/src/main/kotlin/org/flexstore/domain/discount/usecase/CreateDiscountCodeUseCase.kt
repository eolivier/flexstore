package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives a new discount code: well formed, with a valid reduction and usage limit, and a code
// that no other discount code has
class CreateDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : UseCase<DiscountCode> {

    override fun getPreConditions(): PreConditions<DiscountCode> =
        PreConditions(discountCodeIsValidCondition(), codeIsFreeCondition())

    override fun getNominalScenario(): NominalScenario<DiscountCode> =
        NominalScenario(Step { discountCode -> discountCodeRepository.save(discountCode) })

    override fun getPostConditions(): PostConditions<DiscountCode> =
        PostConditions(discountCodeIsStoredCondition())

    private fun discountCodeIsValidCondition() = PreCondition<DiscountCode> { discountCode ->
        val validity = discountCode.offer.validity
        val reason = when {
            discountCode.code.isMalformed() -> "its code is upper-case letters and digits, 3 to 30"
            discountCode.reduction.isInvalid() -> "its reduction is more than 0 (a percentage up to 100)"
            validity.usageLimit.isInvalid() -> "it may be used by 1 order at least"
            else -> null
        }
        if (reason != null) {
            throw InvalidDiscountCodeException(ErrorMessage("The discount code ${discountCode.code.value}: $reason."))
        }
    }

    private fun codeIsFreeCondition() = PreCondition<DiscountCode> { discountCode ->
        if (discountCodeRepository.codeExists(discountCode.code)) {
            val message = ErrorMessage("The discount code ${discountCode.code.value} already exists.")
            throw DiscountCodeAlreadyExistsException(message)
        }
    }

    private fun discountCodeIsStoredCondition() = PostCondition<DiscountCode> { discountCode ->
        if (discountCodeRepository.codeIsMissing(discountCode.code)) {
            val message = ErrorMessage("The discount code ${discountCode.code.value} was not stored.")
            throw DiscountCodeCreationFailed(message)
        }
    }
}
