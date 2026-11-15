package org.flexstore.domain.preference.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.preference.*
import org.flexstore.domain.preference.UserPreference.DisplayMode
import org.flexstore.domain.preference.UserPreference.GuidedTour
import org.flexstore.domain.preference.UserPreference.PreferredLanguage
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryUserPreferenceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class UserPreferenceUseCasesTest : WithAssertions {

    private val userRepository = customersWithIds("customer-1")
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val customer = actorResolutionService.actorOf(ValidUserId("customer-1"))
    private val userPreferenceRepository = InMemoryUserPreferenceRepository()
    private val readUserPreferenceUseCase = ReadUserPreferenceUseCase(userPreferenceRepository)
    private val recordUserPreferenceUseCase = RecordUserPreferenceUseCase(userPreferenceRepository)
    private val userPreferenceUseCases =
        UserPreferenceUseCases(readUserPreferenceUseCase, recordUserPreferenceUseCase)

    @Test
    fun `a customer who never chose should have no preference`() {
        // Act
        val preferences = userPreferenceUseCases.preferencesOf(customer)
        // Assert
        assertThat(preferences)
            .isEqualTo(
                UserPreferences(
                    ColorModePreference.Unchosen, LanguagePreference.Unchosen, GuidedTourPreference.Unchosen
                )
            )
    }

    @ParameterizedTest(name = "a customer chooses the mode {0}")
    @EnumSource(ColorMode::class)
    fun `a customer should record their display mode`(colorMode: ColorMode) {
        // Act
        val preferences = userPreferenceUseCases.record(customer, DisplayMode(colorMode))
        // Assert
        assertThat(preferences.colorMode).isEqualTo(ColorModePreference.Chosen(colorMode))
    }

    @ParameterizedTest(name = "a customer chooses the language {0}")
    @EnumSource(Language::class)
    fun `a customer should record their preferred language`(language: Language) {
        // Act
        val preferences = userPreferenceUseCases.record(customer, PreferredLanguage(language))
        // Assert
        assertThat(preferences.language).isEqualTo(LanguagePreference.Chosen(language))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        userPreferenceUseCases.record(customer, DisplayMode(ColorMode.DARK))
        userPreferenceUseCases.record(customer, PreferredLanguage(Language.FR))
        // Act
        val preferences = userPreferenceUseCases.record(customer, DisplayMode(ColorMode.LIGHT))
        // Assert
        assertThat(preferences.colorMode).isEqualTo(ColorModePreference.Chosen(ColorMode.LIGHT))
    }

    @Test
    fun `choosing a language should keep the display mode`() {
        // Arrange
        userPreferenceUseCases.record(customer, DisplayMode(ColorMode.DARK))
        // Act
        val preferences = userPreferenceUseCases.record(customer, PreferredLanguage(Language.FR))
        // Assert
        assertThat(preferences).isEqualTo(
            UserPreferences(
                ColorModePreference.Chosen(ColorMode.DARK),
                LanguagePreference.Chosen(Language.FR),
                GuidedTourPreference.Unchosen
            )
        )
    }

    @ParameterizedTest(name = "a customer chooses the guided tour {0}")
    @EnumSource(GuidedTourDisplay::class)
    fun `a customer should record whether the guided tour is shown`(guidedTourDisplay: GuidedTourDisplay) {
        // Act
        val preferences = userPreferenceUseCases.record(customer, GuidedTour(guidedTourDisplay))
        // Assert
        assertThat(preferences.guidedTour).isEqualTo(GuidedTourPreference.Chosen(guidedTourDisplay))
    }

    @Test
    fun `showing the guided tour again should replace hiding it`() {
        // Arrange
        userPreferenceUseCases.record(customer, GuidedTour(GuidedTourDisplay.HIDDEN))
        // Act
        val preferences = userPreferenceUseCases.record(customer, GuidedTour(GuidedTourDisplay.SHOWN))
        // Assert
        assertThat(preferences.guidedTour).isEqualTo(GuidedTourPreference.Chosen(GuidedTourDisplay.SHOWN))
    }

    @Test
    fun `hiding the guided tour should keep the display mode`() {
        // Arrange
        userPreferenceUseCases.record(customer, DisplayMode(ColorMode.DARK))
        // Act
        val preferences = userPreferenceUseCases.record(customer, GuidedTour(GuidedTourDisplay.HIDDEN))
        // Assert
        assertThat(preferences.colorMode).isEqualTo(ColorModePreference.Chosen(ColorMode.DARK))
    }

    @Test
    fun `an unknown user should not record a preference`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.record(unknownUser, DisplayMode(ColorMode.DARK)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read preferences`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.preferencesOf(unknownUser) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not record a preference`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.record(blockedCustomer, DisplayMode(ColorMode.DARK)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not read preferences`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        // Act
        val thrown = catchThrowable { userPreferenceUseCases.preferencesOf(blockedCustomer) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
