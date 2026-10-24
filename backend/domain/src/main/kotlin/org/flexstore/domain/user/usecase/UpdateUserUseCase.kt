package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

class UpdateUserUseCase(private val userRepository: UserRepository) : UseCase<User> {

    override fun getPreConditions(): PreConditions<User> {
        println("#[BEGIN] UpdateUserUseCase.getPreConditions")
        val preConditions = PreConditions(isValidUserIdCondition(), userDoExistCondition())
        println("#[END] UpdateUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("#[BEGIN] UpdateUserUseCase.getNominalScenario")
        // Steps
        val updateUserStep = Step<User> { user -> userRepository.save(keepStoredFields(user)) }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(updateUserStep))
        println("#[END] UpdateUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] UpdateUserUseCase.getPostConditions")
        val postConditions = listOf<EmptyPostCondition<User>>(EmptyPostCondition())
        println("#[END] UpdateUserUseCase.getPostConditions")
        return postConditions
    }

    // The role cannot be changed by an update, and a blank password means the password is not updated:
    // the stored role and (hashed) password are kept
    private fun keepStoredFields(user: User): User {
        if (user !is User.DefinedUser) {
            return user
        }
        return when (val existingUser = userRepository.findById(user.id)) {
            is User.DefinedUser -> {
                val passwordNotProvided = user.password is PlainPassword && user.password.value.isBlank()
                user.copy(
                    role = existingUser.role,
                    password = if (passwordNotProvided) existingUser.password else user.password
                )
            }
            else -> user
        }
    }

    private fun isValidUserIdCondition() = PreCondition<User> { user ->
        if (user.id.isInvalid()) {
            throw InvalidUserIdException(ErrorMessage("User ID ${user.id.value} is invalid."))
        }
    }

    private fun userDoExistCondition() = PreCondition<User> { user ->
        if (userRepository.notExists(user.id)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${user.id.value} does not exist."))
        }
    }
}