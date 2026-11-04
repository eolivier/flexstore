package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.twofactor.InvalidTwoFactorAuthenticationChangeException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationForbiddenException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationNotEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class AdminDisableTwoFactorAuthenticationUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = AdminDisableTwoFactorAuthenticationUseCase(userRepository)
    private val administrator = User.DefinedUser(
        UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")),
        Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN
    )
    private val otherAdmin = User.DefinedUser(
        UserId.ValidUserId("admin-2"), FullName(FirstName("Other"), LastName("Admin")),
        Email("other@example.com"), PlainPassword("other-password"), Role.ADMIN,
        twoFactorAuthentication = TwoFactorAuthentication.Enabled(TwoFactorSecret.next())
    )

    init {
        listOf(administrator, otherAdmin).forEach { userRepository.save(it) }
    }

    private fun storedTwoFactorAuthenticationOf(userId: UserId) =
        (userRepository.findById(userId) as User.DefinedUser).twoFactorAuthentication

    @Test
    fun `an administrator should disable the two-factor authentication of another user`() {
        // Act
        useCase.unfold(AdminTwoFactorDisabling(administrator.id, otherAdmin.id))
        // Assert
        assertThat(storedTwoFactorAuthenticationOf(otherAdmin.id)).isEqualTo(TwoFactorAuthentication.Disabled)
    }

    @Test
    fun `only an administrator should disable the two-factor authentication of a user`() {
        // Arrange
        val jane = User.DefinedUser(
            UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")),
            Email("jane@example.com"), PlainPassword("password123")
        )
        userRepository.save(jane)
        // Act
        val thrown = catchThrowable { useCase.unfold(AdminTwoFactorDisabling(jane.id, otherAdmin.id)) }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorAuthenticationForbiddenException::class.java)
        assertThat(storedTwoFactorAuthenticationOf(otherAdmin.id)).isInstanceOf(
            TwoFactorAuthentication.Enabled::class.java
        )
    }

    @Test
    fun `an administrator should not disable the two-factor authentication of their own account this way`() {
        // Arrange
        val selfAdministrator = administrator.copy(
            twoFactorAuthentication = TwoFactorAuthentication.Enabled(TwoFactorSecret.next())
        )
        userRepository.save(selfAdministrator)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(AdminTwoFactorDisabling(selfAdministrator.id, selfAdministrator.id))
        }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidTwoFactorAuthenticationChangeException::class.java)
        assertThat(storedTwoFactorAuthenticationOf(selfAdministrator.id)).isInstanceOf(
            TwoFactorAuthentication.Enabled::class.java
        )
    }

    @Test
    fun `the two-factor authentication of an unknown user should not be disabled`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(AdminTwoFactorDisabling(administrator.id, UserId.ValidUserId("unknown")))
        }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `the two-factor authentication of a user should not be disabled when it is already disabled`() {
        // Arrange
        val adminWithoutTwoFactor = User.DefinedUser(
            UserId.ValidUserId("admin-3"), FullName(FirstName("Third"), LastName("Admin")),
            Email("third@example.com"), PlainPassword("third-password"), Role.ADMIN
        )
        userRepository.save(adminWithoutTwoFactor)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(AdminTwoFactorDisabling(administrator.id, adminWithoutTwoFactor.id))
        }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorAuthenticationNotEnabledException::class.java)
    }
}
