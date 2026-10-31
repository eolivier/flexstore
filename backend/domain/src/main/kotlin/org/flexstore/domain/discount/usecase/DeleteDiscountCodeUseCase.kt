package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator deletes a discount code: the orders that used it keep their discount
class DeleteDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : UseCase<PromoCode> {

    override fun getPreConditions(): PreConditions<PromoCode> = PreConditions(codeExistsCondition())

    override fun getNominalScenario(): NominalScenario<PromoCode> =
        NominalScenario(Step { code -> discountCodeRepository.delete(code) })

    override fun getPostConditions(): PostConditions<PromoCode> = PostConditions.none()

    private fun codeExistsCondition() = PreCondition<PromoCode> { code ->
        if (discountCodeRepository.codeIsMissing(code)) {
            throw DiscountCodeNotFoundException(ErrorMessage("No discount code ${code.value}."))
        }
    }
}
