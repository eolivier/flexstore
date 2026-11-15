package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A user is created by an administrator: the creator is the authenticated user who asks for it
data class CreateUserRequest(val creator: Actor, val user: User)

class CreateUserUseCase(
    private val userRepository: UserRepository
) : UseCase<CreateUserRequest> {

    override fun getPreConditions(): PreConditions<CreateUserRequest> {
        return PreConditions(
            creatorIsAdministratorCondition(),
            roleIsNotSystemCondition(),
            userDoesNotExistCondition(),
            passwordIsNotBlankCondition()
        )
    }

    override fun getNominalScenario(): NominalScenario<CreateUserRequest> {
        // Steps
        val createUserStep = Step<CreateUserRequest> { request ->
            // Repository will handle password hashing
            userRepository.save(request.user)
        }
        // Nominal scenario
        return NominalScenario(listOf(createUserStep))
    }

    override fun getPostConditions(): PostConditions<CreateUserRequest> {
        return PostConditions(userExistsCondition())
    }

    // The creator, resolved before the use case runs, must be an Admin
    private fun creatorIsAdministratorCondition() = PreCondition<CreateUserRequest> { request ->
        if (request.creator.isNotAdmin()) {
            throw UserCreationForbiddenException(ErrorMessage("Only an administrator can create a user."))
        }
    }

    // The system user exists once, created by a migration: an administrator cannot create another one
    private fun roleIsNotSystemCondition() = PreCondition<CreateUserRequest> { request ->
        val user = request.user
        if (user is User.DraftUser && user.role.isSystem()) {
            throw SystemRoleForbiddenException(ErrorMessage("The SYSTEM role cannot be given to a user."))
        }
    }

    private fun userDoesNotExistCondition() = PreCondition<CreateUserRequest> { request ->
        when (val user = request.user) {
            // A draft user has no ID yet: uniqueness is checked on the email
            is User.DraftUser -> if (userRepository.emailIsAlreadyUsed(user.email)) {
                throw UserAlreadyExists(ErrorMessage("User with email ${user.email.value} already exists."))
            }
            else -> if (userRepository.exists(user.id)) {
                throw UserAlreadyExists(ErrorMessage("User with ID ${user.id.value} already exists."))
            }
        }
    }

    private fun passwordIsNotBlankCondition() = PreCondition<CreateUserRequest> { request ->
        val user = request.user
        if (user is User.DraftUser && user.password.value.isBlank()) {
            throw InvalidPasswordException(ErrorMessage("Password must not be blank."))
        }
    }

    // A draft user only gets its ID from the repository: it is looked for by its email
    private fun userExistsCondition() = PostCondition<CreateUserRequest> { request ->
        when (val user = request.user) {
            is User.DraftUser -> if (userRepository.notExistsBasedOn(user.email)) {
                throw UserCreationFailed(ErrorMessage("User with email ${user.email.value} was not created."))
            }
            else -> if (userRepository.notExists(user.id)) {
                throw UserCreationFailed(ErrorMessage("User with ID ${user.id.value} was not created."))
            }
        }
    }
}