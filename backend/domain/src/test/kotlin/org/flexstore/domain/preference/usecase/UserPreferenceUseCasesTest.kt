package org.flexstore.domain.preference.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.preference.*
import org.flexstore.domain.preference.UserPreference.DisplayMode
import org.flexstore.domain.preference.UserPreference.PreferredLanguage
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryUserPreferenceRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class UserPreferenceUseCasesTest : WithAssertions {

    private val customer = DefinedUser(
        ValidUserId("customer-1"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane@flexstore.fr"),
        HashedPassword("hashed"), Role.USER
    )
    private val userRepository = InMemoryUserRepository().apply { save(customer) }
    private val userPreferenceRepository = InMemoryUserPreferenceRepository()
    private val readUserPreferenceUseCase =
        ReadUserPreferenceUseCase(userPreferenceRepository, userRepository)
    private val recordUserPreferenceUseCase =
        RecordUserPreferenceUseCase(userPreferenceRepository, userRepository)
    private val userPreferenceUseCases =
        UserPreferenceUseCases(readUserPreferenceUseCase, recordUserPreferenceUseCase)

    @Test
    fun `a customer who never chose should have no preference`() {
        // Act
        val preferences = userPreferenceUseCases.preferencesOf(customer.id)
        // Assert
        assertThat(preferences)
            .isEqualTo(UserPreferences(ColorModePreference.Unchosen, LanguagePreference.Unchosen))
    }

    @ParameterizedTest(name = "a customer chooses the mode {0}")
    @EnumSource(ColorMode::class)
    fun `a customer should record their display mode`(colorMode: ColorMode) {
        // Act
        val preferences = userPreferenceUseCases.record(customer.id, DisplayMode(colorMode))
        // Assert
        assertThat(preferences.colorMode).isEqualTo(ColorModePreference.Chosen(colorMode))
    }

    @ParameterizedTest(name = "a customer chooses the language {0}")
    @EnumSource(Language::class)
    fun `a customer should record their preferred language`(language: Language) {
        // Act
        val preferences = userPreferenceUseCases.record(customer.id, PreferredLanguage(language))
        // Assert
        assertThat(preferences.language).isEqualTo(LanguagePreference.Chosen(language))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        userPreferenceUseCases.record(customer.id, DisplayMode(ColorMode.DARK))
        userPreferenceUseCases.record(customer.id, PreferredLanguage(Language.FR))
        // Act
        val preferences = userPreferenceUseCases.record(customer.id, DisplayMode(ColorMode.LIGHT))
        // Assert
        assertThat(preferences.colorMode).isEqualTo(ColorModePreference.Chosen(ColorMode.LIGHT))
    }

    @Test
    fun `choosing a language should keep the display mode`() {
        // Arrange
        userPreferenceUseCases.record(customer.id, DisplayMode(ColorMode.DARK))
        // Act
        val preferences = userPreferenceUseCases.record(customer.id, PreferredLanguage(Language.FR))
        // Assert
        assertThat(preferences).isEqualTo(
            UserPreferences(ColorModePreference.Chosen(ColorMode.DARK), LanguagePreference.Chosen(Language.FR))
        )
    }

    @Test
    fun `an unknown user should not record a preference`() {
        // Arrange
        val unknownUser = ValidUserId("unknown")
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.record(unknownUser, DisplayMode(ColorMode.DARK)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read preferences`() {
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.preferencesOf(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
