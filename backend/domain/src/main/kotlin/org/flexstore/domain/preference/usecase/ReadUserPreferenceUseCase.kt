package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.entity.*

// A signed-in customer reads their preferences (their display mode, their language and their guided tour)
class ReadUserPreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository
) : QueryUseCase<UserPreferencesReading, UserPreferences> {

    override fun getPreConditions(): PreConditions<UserPreferencesReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() =
        NominalQuery<UserPreferencesReading, UserPreferences> { reading ->
            userPreferenceRepository.preferencesOf(reading.customer.customerId())
        }

    override fun getPostConditions(): PostConditions<UserPreferences> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<UserPreferencesReading> { reading ->
        reading.customer.verifyCanAct()
    }
}
