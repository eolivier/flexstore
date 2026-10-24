package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.ucop.domain.NominalException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class CreateUserUseCase(
    private val userRepository: UserRepository
) : UseCase<User> {

    lateinit var createdUser: User

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] CreateUserUseCase.getPreConditions")
        val preConditions = listOf(userDoesNotExistCondition(), passwordIsNotBlankCondition())
        println("#[END] CreateUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("##[BEGIN] CreateUserUseCase.getNominalScenario")
        // Steps
        val createUserStep = Step<User> { user -> 
            // Repository will handle password hashing
            createdUser = userRepository.save(user)
        }
        // Nominal scenario
        val nominalScenario = NominalScenario(listOf(createUserStep))
        println("##[END] CreateUserUseCase.getNominalScenario")
        return nominalScenario
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        println("#[BEGIN] CreateUserUseCase.getPostConditions")
        val postConditions = listOf(userExistsCondition())
        println("#[END] CreateUserUseCase.getPostConditions")
        return postConditions
    }

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<User>> {
        return emptyMap()
    }

    private fun userDoesNotExistCondition() = PreCondition<User> { user ->
        when (user) {
            // A draft user has no ID yet: uniqueness is checked on the email
            is User.DraftUser -> if (userRepository.emailIsAlreadyUsed(user.email)) {
                throw UserAlreadyExists(ErrorMessage("User with email ${user.email.value} already exists."))
            }
            else -> if (userRepository.exists(user.id)) {
                throw UserAlreadyExists(ErrorMessage("User with ID ${user.id.value} already exists."))
            }
        }
    }

    private fun passwordIsNotBlankCondition() = PreCondition<User> { user ->
        if (user is User.DraftUser && user.password.value.isBlank()) {
            throw InvalidPasswordException(ErrorMessage("Password must not be blank."))
        }
    }

    // Checked on the saved user, since a draft user only gets its ID from the repository
    private fun userExistsCondition(): PostCondition<User> = PostCondition<User> {
        if (userRepository.notExists(createdUser.id)) {
            throw UserCreationFailed(ErrorMessage("User with ID ${createdUser.id.value} was not created."))
        }
    }
}