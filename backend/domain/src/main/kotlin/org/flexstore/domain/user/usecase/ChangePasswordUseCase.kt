package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.ucop.domain.NominalException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

const val PASSWORD_MIN_LENGTH = 8

data class ChangePasswordRequest(val userId: UserId, val currentPassword: PlainPassword, val newPassword: PlainPassword)

class ChangePasswordUseCase(
    private val userRepository: UserRepository
) : UseCase<ChangePasswordRequest> {

    override fun getPreConditions(): List<PreCondition<ChangePasswordRequest>> {
        println("#[BEGIN] ChangePasswordUseCase.getPreConditions")
        val preConditions = listOf(userExistsCondition(), currentPasswordMatchesCondition(), newPasswordIsLongEnoughCondition())
        println("#[END] ChangePasswordUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<ChangePasswordRequest> {
        println("#[BEGIN] ChangePasswordUseCase.getNominalScenario")
        // Steps
        val changePasswordStep = Step<ChangePasswordRequest> { request ->
            // Repository will handle password hashing
            userRepository.save(definedUser(request).copy(password = request.newPassword))
        }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(changePasswordStep))
        println("#[END] ChangePasswordUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<ChangePasswordRequest>> {
        println("#[BEGIN] ChangePasswordUseCase.getPostConditions")
        val postConditions = listOf(newPasswordMatchesCondition())
        println("#[END] ChangePasswordUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<ChangePasswordRequest>> {
        return emptyMap()
    }

    private fun definedUser(request: ChangePasswordRequest): User.DefinedUser =
        when (val user = userRepository.findById(request.userId)) {
            is User.DefinedUser -> user
            else -> throw UserNotFoundException(ErrorMessage("User with ID ${request.userId.value} does not exist."))
        }

    private fun userExistsCondition() = PreCondition<ChangePasswordRequest> { request ->
        if (userRepository.notExists(request.userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${request.userId.value} does not exist."))
        }
    }

    private fun currentPasswordMatchesCondition() = PreCondition<ChangePasswordRequest> { request ->
        if (userRepository.passwordDoesNotMatch(definedUser(request).email, request.currentPassword)) {
            throw InvalidCurrentPasswordException(ErrorMessage("The current password is wrong."))
        }
    }

    private fun newPasswordIsLongEnoughCondition() = PreCondition<ChangePasswordRequest> { request ->
        if (request.newPassword.value.length < PASSWORD_MIN_LENGTH) {
            throw InvalidPasswordException(ErrorMessage("Password must be at least $PASSWORD_MIN_LENGTH characters."))
        }
    }

    private fun newPasswordMatchesCondition() = PostCondition<ChangePasswordRequest> { request ->
        if (userRepository.passwordDoesNotMatch(definedUser(request).email, request.newPassword)) {
            throw PasswordChangeFailed(ErrorMessage("Password of user ${request.userId.value} was not changed."))
        }
    }
}

// Not InvalidCredentialsException: the user is authenticated, only the confirmation of the current password failed
data class InvalidCurrentPasswordException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class PasswordChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
