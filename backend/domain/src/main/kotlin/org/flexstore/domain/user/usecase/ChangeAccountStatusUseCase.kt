package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// An administrator (the editor) activates, suspends, locks or disables the account of another user
data class AccountStatusChange(val editor: UserId, val userId: UserId, val status: AccountStatus)

class ChangeAccountStatusUseCase(private val userRepository: UserRepository) : UseCase<AccountStatusChange> {

    override fun getPreConditions(): PreConditions<AccountStatusChange> = PreConditions(
        editorIsAdministratorCondition(),
        accountExistsCondition(),
        accountIsNotTheEditorsCondition(),
        statusCanBeChosenCondition()
    )

    override fun getNominalScenario(): NominalScenario<AccountStatusChange> =
        NominalScenario(Step { change -> userRepository.save(user(change.userId).copy(status = change.status)) })

    override fun getPostConditions(): PostConditions<AccountStatusChange> = PostConditions.none()

    private fun user(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<AccountStatusChange> { change ->
        if (userRepository.isNotAdministrator(change.editor)) {
            val message = ErrorMessage("Only an administrator can change the status of an account.")
            throw UserUpdateForbiddenException(message)
        }
    }

    private fun accountExistsCondition() = PreCondition<AccountStatusChange> { change ->
        if (userRepository.notExists(change.userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${change.userId.value} does not exist."))
        }
    }

    // An administrator cannot lock themselves out
    private fun accountIsNotTheEditorsCondition() = PreCondition<AccountStatusChange> { change ->
        if (change.userId == change.editor) {
            val message = ErrorMessage("An administrator cannot change the status of their own account.")
            throw InvalidStatusChangeException(message)
        }
    }

    private fun statusCanBeChosenCondition() = PreCondition<AccountStatusChange> { change ->
        if (change.status.cannotBeChosenByAnAdministrator()) {
            val message = ErrorMessage("The status ${change.status} is given by the verification of the email only.")
            throw InvalidStatusChangeException(message)
        }
    }
}
