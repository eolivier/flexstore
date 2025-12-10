package org.flexstore.domain.usecase.user

import org.ucop.domain.NominalException
import org.flexstore.domain.entity.*
import org.flexstore.domain.port.PasswordEncoder
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

class CreateUserUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UseCase<User> {

    lateinit var createdUser: User

    override fun getPreConditions(): List<PreCondition<User>> {
        println("#[BEGIN] CreateUserUseCase.getPreConditions")
        val preConditions = listOf(userDoesNotExistCondition())
        println("#[END] CreateUserUseCase.getPreConditions")
        return preConditions
    }

    override fun getNominalScenario(): NominalScenario<User> {
        println("##[BEGIN] CreateUserUseCase.getNominalScenario")
        // Steps
        val createUserStep = Step<User> { user -> 
            // Hash the password before saving
            val hashedUser = when (user) {
                is User.DefinedUser -> {
                    val hashedPassword = Password(passwordEncoder.encode(user.password.value))
                    user.copy(password = hashedPassword)
                }
                else -> user
            }
            createdUser = userRepository.save(hashedUser)
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
        if (userRepository.exists(user.id)) {
            throw UserAlreadyExists(NonEmptyString("User with ID ${user.id.value} already exists."))
        }
    }

    private fun userExistsCondition(): PostCondition<User> = PostCondition<User> { user ->
        if (userRepository.notExists(user.id)) {
            throw UserCreationFailed(NonEmptyString("User with ID ${user.id.value} was not created."))
        }
    }
}