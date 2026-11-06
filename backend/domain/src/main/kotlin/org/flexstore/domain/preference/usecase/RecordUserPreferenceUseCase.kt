package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer chooses their display mode: it is kept in their account for their next visits
class RecordUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : UseCase<ColorModeChoice> {

    override fun getPreConditions(): PreConditions<ColorModeChoice> =
        PreConditions(customerExistsCondition())

    override fun getNominalScenario(): NominalScenario<ColorModeChoice> =
        NominalScenario(Step { colorModeChoice ->
            userPreferenceRepository.save(colorModeChoice.customer, colorModeChoice.colorMode)
        })

    override fun getPostConditions(): PostConditions<ColorModeChoice> =
        PostConditions(preferenceIsStoredCondition())

    private fun customerExistsCondition() = PreCondition<ColorModeChoice> { colorModeChoice ->
        if (userRepository.notExists(colorModeChoice.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${colorModeChoice.customer.value} not found."))
        }
    }

    private fun preferenceIsStoredCondition() = PostCondition<ColorModeChoice> { colorModeChoice ->
        val storedPreference = userPreferenceRepository.preferenceOf(colorModeChoice.customer)
        if (storedPreference != ColorModePreference.Chosen(colorModeChoice.colorMode)) {
            throw ColorModePreferenceRecordFailed(ErrorMessage("The display mode was not recorded."))
        }
    }
}
