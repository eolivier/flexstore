package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer reads their preferences (their display mode, their language and their guided tour)
class ReadUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : QueryUseCase<UserPreferencesReading, UserPreferences> {

    override fun getPreConditions(): PreConditions<UserPreferencesReading> =
        PreConditions(customerExistsCondition())

    override fun getNominalQuery() =
        NominalQuery<UserPreferencesReading, UserPreferences> { reading ->
            userPreferenceRepository.preferencesOf(reading.customer)
        }

    override fun getPostConditions(): PostConditions<UserPreferences> = PostConditions.none()

    private fun customerExistsCondition() = PreCondition<UserPreferencesReading> { reading ->
        if (userRepository.notExists(reading.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${reading.customer.value} not found."))
        }
    }
}
