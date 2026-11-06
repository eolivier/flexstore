package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A signed-in customer chooses their preferred language: it is kept in their account for their next visits
class RecordLanguagePreferenceUseCase(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) : UseCase<LanguageChoice> {

    override fun getPreConditions(): PreConditions<LanguageChoice> =
        PreConditions(customerExistsCondition())

    override fun getNominalScenario(): NominalScenario<LanguageChoice> =
        NominalScenario(Step { languageChoice ->
            userPreferenceRepository.saveLanguage(languageChoice.customer, languageChoice.language)
        })

    override fun getPostConditions(): PostConditions<LanguageChoice> =
        PostConditions(languageIsStoredCondition())

    private fun customerExistsCondition() = PreCondition<LanguageChoice> { languageChoice ->
        if (userRepository.notExists(languageChoice.customer)) {
            throw UserNotFoundException(ErrorMessage("User ${languageChoice.customer.value} not found."))
        }
    }

    private fun languageIsStoredCondition() = PostCondition<LanguageChoice> { languageChoice ->
        val storedPreference = userPreferenceRepository.languagePreferenceOf(languageChoice.customer)
        if (storedPreference != LanguagePreference.Chosen(languageChoice.language)) {
            throw LanguagePreferenceRecordFailed(ErrorMessage("The preferred language was not recorded."))
        }
    }
}
