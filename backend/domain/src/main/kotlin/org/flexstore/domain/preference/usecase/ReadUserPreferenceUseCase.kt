package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer reads their display mode
class ReadUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : QueryUseCase<ColorModePreferenceReading, ColorModePreference> {

    override fun getPreConditions(): PreConditions<ColorModePreferenceReading> =
        PreConditions(customerExistsCondition())

    override fun getNominalQuery() =
        NominalQuery<ColorModePreferenceReading, ColorModePreference> { reading ->
            userPreferenceRepository.preferenceOf(reading.customer)
        }

    override fun getPostConditions(): PostConditions<ColorModePreference> = PostConditions.none()

    private fun customerExistsCondition() = PreCondition<ColorModePreferenceReading> { reading ->
        if (userRepository.notExists(reading.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${reading.customer.value} not found."))
        }
    }
}
