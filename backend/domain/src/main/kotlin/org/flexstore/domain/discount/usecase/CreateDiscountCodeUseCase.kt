package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives a new discount code: well formed, with a valid reduction and usage limit, and a code
// that no other discount code has
class CreateDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : UseCase<DiscountCodeCreation> {

    override fun getPreConditions(): PreConditions<DiscountCodeCreation> =
        PreConditions(creatorIsAdministratorCondition(), discountCodeIsValidCondition(), codeIsFreeCondition())

    override fun getNominalScenario(): NominalScenario<DiscountCodeCreation> =
        NominalScenario(Step { creation -> discountCodeRepository.save(creation.discountCode) })

    override fun getPostConditions(): PostConditions<DiscountCodeCreation> =
        PostConditions(discountCodeIsStoredCondition())

    // The creator, resolved before the use case runs, must be an Admin
    private fun creatorIsAdministratorCondition() = PreCondition<DiscountCodeCreation> { creation ->
        if (creation.creator.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can give a discount code.")
            throw DiscountCodeAdministrationForbiddenException(message)
        }
    }

    private fun discountCodeIsValidCondition() = PreCondition<DiscountCodeCreation> { (_, discountCode) ->
        val validity = discountCode.offer.validity
        val code = discountCode.code
        if (code.isMalformed()) throw invalidDiscountCode(code, "its code is upper-case letters and digits, 3 to 30")
        if (discountCode.reduction.isInvalid()) {
            throw invalidDiscountCode(code, "its reduction is more than 0 (a percentage up to 100)")
        }
        if (validity.usageLimit.isInvalid()) throw invalidDiscountCode(code, "it may be used by 1 order at least")
    }

    private fun invalidDiscountCode(code: PromoCode, reason: String) =
        InvalidDiscountCodeException(ErrorMessage("The discount code ${code.value}: $reason."))

    private fun codeIsFreeCondition() = PreCondition<DiscountCodeCreation> { (_, discountCode) ->
        if (discountCodeRepository.codeExists(discountCode.code)) {
            val message = ErrorMessage("The discount code ${discountCode.code.value} already exists.")
            throw DiscountCodeAlreadyExistsException(message)
        }
    }

    private fun discountCodeIsStoredCondition() = PostCondition<DiscountCodeCreation> { (_, discountCode) ->
        if (discountCodeRepository.codeIsMissing(discountCode.code)) {
            val message = ErrorMessage("The discount code ${discountCode.code.value} was not stored.")
            throw DiscountCodeCreationFailed(message)
        }
    }
}
