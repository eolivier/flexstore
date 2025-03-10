package org.flexstore.domain.usecase

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository

class CreateUserUseCase(private val userRepository: UserRepository) {

    fun perform(newUser: User) {
        // Preconditions
        val userDoesNotExistCondition = PreCondition<User> {
            user -> assert(userRepository.notExists(user.id)) { "User with ID ${user.id.value} already exists." }
        }
        // Steps
        val createUserStep = Step<User> { user -> userRepository.save(user) }
        // Postconditions
        val userExistsCondition = PostCondition<User> {
            user -> assert(userRepository.exists(user.id)) { "User with ID ${user.id.value} was not created." }
        }
        // Nominal scenario
        val createUserScenario = NominalScenario(listOf(createUserStep))
        // Use case
        val createUserUseCase = UseCase(
            preConditions = listOf(userDoesNotExistCondition),
            nominalScenario = createUserScenario,
            postConditions = listOf(userExistsCondition)
        )
        // Run use case
        createUserUseCase.run(newUser)
    }
}