package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An order is placed with a discount code: the code counts one more use (its usage limit is checked before)
class UseDiscountCodeUseCase(private val discountCodeRepository: DiscountCodeRepository) : UseCase<PromoCode> {

    override fun getPreConditions(): PreConditions<PromoCode> =
        PreConditions(codeExistsCondition(), codeIsNotUsedUpCondition())

    override fun getNominalScenario(): NominalScenario<PromoCode> = NominalScenario(Step { code ->
        discountCodeRepository.save(discountCodeOf(code).usedOnceMore())
    })

    override fun getPostConditions(): PostConditions<PromoCode> = PostConditions.none()

    // Only called once the code is known
    private fun discountCodeOf(code: PromoCode) = discountCodeRepository.findBy(code)!!

    private fun codeExistsCondition() = PreCondition<PromoCode> { code ->
        if (discountCodeRepository.codeIsMissing(code)) {
            throw DiscountCodeNotFoundException(ErrorMessage("No discount code ${code.value}."))
        }
    }

    // Another order may have taken the last use since the code was read
    private fun codeIsNotUsedUpCondition() = PreCondition<PromoCode> { code ->
        if (discountCodeOf(code).isUsedUp()) {
            throw DiscountCodeUsedUpException(ErrorMessage("The discount code ${code.value} is used up."))
        }
    }
}
