package org.flexstore.domain.preference.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryColorModePreferenceRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ColorModePreferenceUseCasesTest : WithAssertions {

    private val customer = DefinedUser(
        ValidUserId("customer-1"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane@flexstore.fr"),
        HashedPassword("hashed"), Role.USER
    )
    private val userRepository = InMemoryUserRepository().apply { save(customer) }
    private val colorModePreferenceRepository = InMemoryColorModePreferenceRepository()
    private val readColorModePreferenceUseCase =
        ReadColorModePreferenceUseCase(colorModePreferenceRepository, userRepository)
    private val recordColorModePreferenceUseCase =
        RecordColorModePreferenceUseCase(colorModePreferenceRepository, userRepository)
    private val colorModePreferenceUseCases =
        ColorModePreferenceUseCases(readColorModePreferenceUseCase, recordColorModePreferenceUseCase)

    @Test
    fun `a customer who never chose should have no preference`() {
        // Act
        val colorModePreference = colorModePreferenceUseCases.preferenceOf(customer.id)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Unchosen)
    }

    @ParameterizedTest(name = "a customer chooses the mode {0}")
    @EnumSource(ColorMode::class)
    fun `a customer should record their display mode`(colorMode: ColorMode) {
        // Act
        val colorModePreference = colorModePreferenceUseCases.record(customer.id, colorMode)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Chosen(colorMode))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        colorModePreferenceUseCases.record(customer.id, ColorMode.DARK)
        // Act
        val colorModePreference = colorModePreferenceUseCases.record(customer.id, ColorMode.LIGHT)
        // Assert
        assertThat(colorModePreference).isEqualTo(ColorModePreference.Chosen(ColorMode.LIGHT))
    }

    @Test
    fun `an unknown user should not record a display mode`() {
        // Arrange
        val unknownUser = ValidUserId("unknown")
        // Act
        val thrown = catchThrowable { colorModePreferenceUseCases.record(unknownUser, ColorMode.DARK) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read a display mode`() {
        // Act
        val thrown = catchThrowable { colorModePreferenceUseCases.preferenceOf(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
