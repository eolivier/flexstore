package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator deletes a discount code: the orders that used it keep their discount
class DeleteDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : UseCase<DiscountCodeDeletion> {

    override fun getPreConditions(): PreConditions<DiscountCodeDeletion> =
        PreConditions(editorIsAdministratorCondition(), codeExistsCondition())

    override fun getNominalScenario(): NominalScenario<DiscountCodeDeletion> =
        NominalScenario(Step { deletion -> discountCodeRepository.delete(deletion.code) })

    override fun getPostConditions(): PostConditions<DiscountCodeDeletion> = PostConditions.none()

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<DiscountCodeDeletion> { deletion ->
        if (deletion.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can delete a discount code.")
            throw DiscountCodeAdministrationForbiddenException(message)
        }
    }

    private fun codeExistsCondition() = PreCondition<DiscountCodeDeletion> { (_, code) ->
        if (discountCodeRepository.codeIsMissing(code)) {
            throw DiscountCodeNotFoundException(ErrorMessage("No discount code ${code.value}."))
        }
    }
}
