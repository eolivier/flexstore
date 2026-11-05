package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.repository.FailedSignInRepository
import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*

// An account signed in with the right password: its failed sign-ins in a row are counted again from none
class ResetFailedSignInsUseCase(
    private val failedSignInRepository: FailedSignInRepository
) : UseCase<UserId> {

    override fun getPreConditions(): PreConditions<UserId> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<UserId> =
        NominalScenario(Step { userId -> failedSignInRepository.reset(userId) })

    override fun getPostConditions(): PostConditions<UserId> = PostConditions(countIsNoneCondition())

    private fun countIsNoneCondition() = PostCondition<UserId> { userId ->
        if (failedSignInRepository.countOf(userId) != FailedSignInCount.none()) {
            throw FailedSignInsNotReset(ErrorMessage("The failed sign-ins of ${userId.value} were not reset."))
        }
    }
}

data class FailedSignInsNotReset(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
