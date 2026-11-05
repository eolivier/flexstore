package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator deletes a discount code: the orders that used it keep their discount
class DeleteDiscountCodeUseCase(
    private val discountCodeRepository: DiscountCodeRepository,
    private val userRepository: UserRepository
) : UseCase<DiscountCodeDeletion> {

    override fun getPreConditions(): PreConditions<DiscountCodeDeletion> =
        PreConditions(editorIsAdministratorCondition(), codeExistsCondition())

    override fun getNominalScenario(): NominalScenario<DiscountCodeDeletion> =
        NominalScenario(Step { deletion -> discountCodeRepository.delete(deletion.code) })

    override fun getPostConditions(): PostConditions<DiscountCodeDeletion> = PostConditions.none()

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<DiscountCodeDeletion> { deletion ->
        if (userRepository.isNotAdministrator(deletion.editor)) {
            throw DiscountCodeAdministrationForbiddenException(
                ErrorMessage("Only an administrator can delete a discount code.")
            )
        }
    }

    private fun codeExistsCondition() = PreCondition<DiscountCodeDeletion> { (_, code) ->
        if (discountCodeRepository.codeIsMissing(code)) {
            throw DiscountCodeNotFoundException(ErrorMessage("No discount code ${code.value}."))
        }
    }
}
