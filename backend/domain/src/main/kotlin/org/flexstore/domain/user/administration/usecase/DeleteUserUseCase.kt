package org.flexstore.domain.user.administration.usecase

import kotlin.reflect.KClass
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*

// An administrator who deletes a user
data class UserDeletion(val editor: Actor, val userId: UserId)

class DeleteUserUseCase(private val userRepository: UserRepository) : UseCase<UserDeletion> {

    override fun getPreConditions(): PreConditions<UserDeletion> {
        return PreConditions(
            editorIsAdministratorCondition(), userExistsCondition(), userIsNotTheSystemCondition()
        )
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

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<UserDeletion> { deletion ->
        if (deletion.editor.isNotAdmin()) {
            throw UserDeletionForbiddenException(ErrorMessage("Only an administrator can delete a user."))
        }
    }

    private fun userExistsCondition() = PreCondition<UserDeletion> { (_, userId) ->
        if (userRepository.notExists(userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${userId.value} does not exist."))
        }
    }

    // The system user runs the scheduled use cases: it is never deleted
    private fun userIsNotTheSystemCondition() = PreCondition<UserDeletion> { (_, userId) ->
        val user = userRepository.findById(userId)
        if (user is User.DefinedUser && user.isSystem()) {
            throw SystemRoleForbiddenException(ErrorMessage("The system user cannot be deleted."))
        }
    }

    private fun userDeletedCondition() = PostCondition<UserDeletion> { (_, userId) ->
        if (userRepository.exists(userId)) {
            throw UserDeletionFailed(ErrorMessage("User with ID ${userId.value} was not deleted."))
        }
    }
}