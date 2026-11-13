package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer chooses a preference (their display mode, their language or their guided tour): it is
// kept in their account for their next visits
class RecordUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : UseCase<UserPreferenceChoice> {

    override fun getPreConditions(): PreConditions<UserPreferenceChoice> =
        PreConditions(customerExistsCondition())

    override fun getNominalScenario(): NominalScenario<UserPreferenceChoice> =
        NominalScenario(Step { choice ->
            userPreferenceRepository.save(choice.customer, choice.preference)
        })

    override fun getPostConditions(): PostConditions<UserPreferenceChoice> =
        PostConditions(preferenceIsStoredCondition())

    private fun customerExistsCondition() = PreCondition<UserPreferenceChoice> { choice ->
        if (userRepository.notExists(choice.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${choice.customer.value} not found."))
        }
    }

    private fun preferenceIsStoredCondition() = PostCondition<UserPreferenceChoice> { choice ->
        if (!userPreferenceRepository.preferencesOf(choice.customer).contain(choice.preference)) {
            throw UserPreferenceRecordFailed(ErrorMessage("The preference was not recorded."))
        }
    }
}
