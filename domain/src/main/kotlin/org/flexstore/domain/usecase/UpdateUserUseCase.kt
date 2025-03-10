package org.flexstore.domain.usecase

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository

class UpdateUserUseCase(private val userRepository: UserRepository) : NewUseCase<User> {

    override fun getPreConditions(): List<PreCondition<User>> {
        val userDoExistCondition = PreCondition<User> {
                user -> assert(userRepository.exists(user.id)) { "User with ID ${user.id.value} does not exist." }
        }
        return listOf(userDoExistCondition)
    }

    override fun getNominalScenario(): NominalScenario<User> {
        // Steps
        val updateUserStep = Step<User> { user -> userRepository.save(user) }
        // Nominal scenario
        return NominalScenario(listOf(updateUserStep))
    }

    override fun getPostConditions(): List<PostCondition<User>> {
        return listOf(EmptyPostCondition())
    }
}