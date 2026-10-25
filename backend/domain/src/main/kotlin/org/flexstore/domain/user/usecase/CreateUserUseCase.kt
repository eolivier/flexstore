package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

// A user is created by an administrator: the creator is the authenticated user who asks for it
data class CreateUserRequest(val creator: UserId, val user: User)

class CreateUserUseCase(
    private val userRepository: UserRepository
) : UseCase<CreateUserRequest> {

    lateinit var createdUser: User

    override fun getPreConditions(): PreConditions<CreateUserRequest> {
        return PreConditions(
            creatorIsAdministratorCondition(),
            userDoesNotExistCondition(),
            passwordIsNotBlankCondition()
        )
    }

    override fun getNominalScenario(): NominalScenario<CreateUserRequest> {
        // Steps
        val createUserStep = Step<CreateUserRequest> { request ->
            // Repository will handle password hashing
            createdUser = userRepository.save(request.user)
        }
        // Nominal scenario
        return NominalScenario(listOf(createUserStep))
    }

    override fun getPostConditions(): PostConditions<CreateUserRequest> {
        return PostConditions(userExistsCondition())
    }

    override fun getAlternativeScenarii():
        Map<KClass<out NominalException>, AlternativeScenario<CreateUserRequest>> {
        return emptyMap()
    }

    // The creator must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun creatorIsAdministratorCondition() = PreCondition<CreateUserRequest> { request ->
        if (userRepository.isNotAdministrator(request.creator)) {
            throw UserCreationForbiddenException(ErrorMessage("Only an administrator can create a user."))
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

    // Checked on the saved user, since a draft user only gets its ID from the repository
    private fun userExistsCondition() = PostCondition<CreateUserRequest> {
        if (userRepository.notExists(createdUser.id)) {
            throw UserCreationFailed(ErrorMessage("User with ID ${createdUser.id.value} was not created."))
        }
    }
}