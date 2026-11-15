package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer chooses a preference (their display mode, their language or their guided tour): it is
// kept in their account for their next visits
class RecordUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository
) : UseCase<UserPreferenceChoice> {

    override fun getPreConditions(): PreConditions<UserPreferenceChoice> =
        PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<UserPreferenceChoice> =
        NominalScenario(Step { choice ->
            userPreferenceRepository.save(choice.customer.customerId(), choice.preference)
        })

    override fun getPostConditions(): PostConditions<UserPreferenceChoice> =
        PostConditions(preferenceIsStoredCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<UserPreferenceChoice> { choice ->
        choice.customer.verifyCanAct()
    }

    private fun preferenceIsStoredCondition() = PostCondition<UserPreferenceChoice> { choice ->
        if (userPreferenceRepository.preferencesOf(choice.customer.customerId()).doNotContain(choice.preference)) {
            throw UserPreferenceRecordFailed(ErrorMessage("The preference was not recorded."))
        }
    }
}
