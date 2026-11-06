package org.flexstore.domain.preference.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.preference.*
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
    private val readLanguagePreferenceUseCase =
        ReadLanguagePreferenceUseCase(userPreferenceRepository, userRepository)
    private val recordLanguagePreferenceUseCase =
        RecordLanguagePreferenceUseCase(userPreferenceRepository, userRepository)
    private val userPreferenceUseCases = UserPreferenceUseCases(
        readUserPreferenceUseCase, recordUserPreferenceUseCase,
        readLanguagePreferenceUseCase, recordLanguagePreferenceUseCase
    )

    @Test
    fun `a customer who never chose should have no preference`() {
        // Act
        val colorModePreference = userPreferenceUseCases.preferenceOf(customer.id)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Unchosen)
    }

    @ParameterizedTest(name = "a customer chooses the mode {0}")
    @EnumSource(ColorMode::class)
    fun `a customer should record their display mode`(colorMode: ColorMode) {
        // Act
        val colorModePreference = userPreferenceUseCases.record(customer.id, colorMode)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Chosen(colorMode))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        userPreferenceUseCases.record(customer.id, ColorMode.DARK)
        // Act
        val colorModePreference = userPreferenceUseCases.record(customer.id, ColorMode.LIGHT)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Chosen(ColorMode.LIGHT))
    }

    @Test
    fun `an unknown user should not record a display mode`() {
        // Arrange
        val unknownUser = ValidUserId("unknown")
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.record(unknownUser, ColorMode.DARK) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read a display mode`() {
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.preferenceOf(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `a customer who never chose a language should have no language preference`() {
        // Act
        val languagePreference = userPreferenceUseCases.languagePreferenceOf(customer.id)
        // Assert
        assertThat(languagePreference).isEqualTo(LanguagePreference.Unchosen)
    }

    @ParameterizedTest(name = "a customer chooses the language {0}")
    @EnumSource(Language::class)
    fun `a customer should record their preferred language`(language: Language) {
        // Act
        val languagePreference = userPreferenceUseCases.recordLanguage(customer.id, language)
        // Assert
        assertThat(languagePreference).isEqualTo(LanguagePreference.Chosen(language))
    }

    @Test
    fun `a new language choice should replace the previous one`() {
        // Arrange
        userPreferenceUseCases.recordLanguage(customer.id, Language.FR)
        // Act
        val languagePreference = userPreferenceUseCases.recordLanguage(customer.id, Language.EN)
        // Assert
        assertThat(languagePreference).isEqualTo(LanguagePreference.Chosen(Language.EN))
    }

    @Test
    fun `choosing a language should keep the display mode`() {
        // Arrange
        userPreferenceUseCases.record(customer.id, ColorMode.DARK)
        // Act
        userPreferenceUseCases.recordLanguage(customer.id, Language.FR)
        // Assert
        val colorModePreference = userPreferenceUseCases.preferenceOf(customer.id)
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Chosen(ColorMode.DARK))
    }

    @Test
    fun `an unknown user should not record a language`() {
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.recordLanguage(ValidUserId("unknown"), Language.FR) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read a language`() {
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.languagePreferenceOf(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
