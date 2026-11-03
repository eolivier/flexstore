package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

// An administrator who deletes a user
data class UserDeletion(val editor: UserId, val userId: UserId)

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<UserDeletion> {

    override fun getPreConditions(): PreConditions<UserDeletion> {
        return PreConditions(editorIsAdministratorCondition(), userExistsCondition())
    }

    override fun getNominalScenario(): NominalScenario<UserDeletion> {
        // Steps
        val deleteUserStep = Step<UserDeletion> { (_, userId) -> userRepository.delete(userId) }
        // Nominal scenario
        return NominalScenario(listOf(deleteUserStep))
    }

    override fun getPostConditions(): PostConditions<UserDeletion> {
        return PostConditions(userDeletedCondition())
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<UserDeletion>> {
        return emptyMap()
    }

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<UserDeletion> { deletion ->
        if (userRepository.isNotAdministrator(deletion.editor)) {
            throw UserDeletionForbiddenException(ErrorMessage("Only an administrator can delete a user."))
        }
    }

    private fun userExistsCondition() = PreCondition<UserDeletion> { (_, userId) ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }

    private fun userDeletedCondition() = PostCondition<UserDeletion> { (_, userId) ->
        if (userRepository.exists(userId)) {
            throw UserDeletionFailed(ErrorMessage("User with ID ${userId.value} was not deleted."))
        }
    }
}