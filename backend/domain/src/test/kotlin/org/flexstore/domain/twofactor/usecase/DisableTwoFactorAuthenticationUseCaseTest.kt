package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.twofactor.model.TwoFactorAuthenticationNotEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.domain.user.usecase.InvalidCurrentPasswordException
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class DisableTwoFactorAuthenticationUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = DisableTwoFactorAuthenticationUseCase(userRepository)
    private val password = PlainPassword("admin-password")
    private val administrator = User.DefinedUser(
        UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")),
        Email("admin@example.com"), password, Role.ADMIN,
        twoFactorAuthentication = TwoFactorAuthentication.Enabled(TwoFactorSecret.next())
    )

    init {
        userRepository.save(administrator)
    }

    private fun storedTwoFactorAuthenticationOf(userId: UserId) =
        (userRepository.findById(userId) as User.DefinedUser).twoFactorAuthentication

    @Test
    fun `the current password should disable two-factor authentication`() {
        // Act
        useCase.unfold(TwoFactorDisabling(administrator.id, password))
        // Assert
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isEqualTo(TwoFactorAuthentication.Disabled)
    }

    @Test
    fun `a wrong current password should not disable two-factor authentication`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorDisabling(administrator.id, PlainPassword("wrong-password")))
        }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCurrentPasswordException::class.java)
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isInstanceOf(
            TwoFactorAuthentication.Enabled::class.java
        )
    }

    @Test
    fun `two-factor authentication already disabled should not be disabled again`() {
        // Arrange
        val other = User.DefinedUser(
            UserId.ValidUserId("admin-2"), FullName(FirstName("Other"), LastName("Admin")),
            Email("other@example.com"), PlainPassword("other-password"), Role.ADMIN
        )
        userRepository.save(other)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorDisabling(other.id, PlainPassword("other-password")))
        }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorAuthenticationNotEnabledException::class.java)
    }

    @Test
    fun `a customer should disable their own two-factor authentication`() {
        // Arrange
        val janePassword = PlainPassword("password123")
        val jane = User.DefinedUser(
            UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")),
            Email("jane@example.com"), janePassword,
            twoFactorAuthentication = TwoFactorAuthentication.Enabled(TwoFactorSecret.next())
        )
        userRepository.save(jane)
        // Act
        useCase.unfold(TwoFactorDisabling(jane.id, janePassword))
        // Assert
        assertThat(storedTwoFactorAuthenticationOf(jane.id)).isEqualTo(TwoFactorAuthentication.Disabled)
    }

    @Test
    fun `an unknown user should not disable two-factor authentication`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorDisabling(unknownCustomerId, PlainPassword("password123")))
        }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
