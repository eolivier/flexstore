package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer reads their preferred language
class ReadLanguagePreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : QueryUseCase<LanguagePreferenceReading, LanguagePreference> {

    override fun getPreConditions(): PreConditions<LanguagePreferenceReading> =
        PreConditions(customerExistsCondition())

    override fun getNominalQuery() =
        NominalQuery<LanguagePreferenceReading, LanguagePreference> { reading ->
            userPreferenceRepository.languagePreferenceOf(reading.customer)
        }

    override fun getPostConditions(): PostConditions<LanguagePreference> = PostConditions.none()

    private fun customerExistsCondition() = PreCondition<LanguagePreferenceReading> { reading ->
        if (userRepository.notExists(reading.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${reading.customer.value} not found."))
        }
    }
}
