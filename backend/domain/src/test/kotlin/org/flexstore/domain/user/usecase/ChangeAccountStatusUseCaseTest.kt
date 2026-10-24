package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ChangeAccountStatusUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = ChangeAccountStatusUseCase(userRepository)
    private val administrator = User.DefinedUser(UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")), Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN)
    private val jane = User.DefinedUser(UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password123"))

    init {
        listOf(administrator, jane).forEach { userRepository.save(it) }
    }

    private fun stored() = userRepository.findById(jane.id) as User.DefinedUser

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class, names = ["ACTIVE", "SUSPENDED", "LOCKED", "DISABLED"])
    fun `an administrator should change the status of an account`(status: AccountStatus) {
        // Act
        useCase.unfold(AccountStatusChange(administrator.id, jane.id, status))
        // Assert
        assertThat(stored().status).isEqualTo(status)
    }

    @Test
    fun `the status change should keep the rest of the account`() {
        // Act
        useCase.unfold(AccountStatusChange(administrator.id, jane.id, AccountStatus.SUSPENDED))
        // Assert
        assertThat(stored().fullName).isEqualTo(jane.fullName)
        assertThat(userRepository.passwordMatches(jane.email, PlainPassword("password123"))).isTrue()
    }

    @Test
    fun `only an administrator should change the status of an account`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AccountStatusChange(jane.id, administrator.id, AccountStatus.SUSPENDED)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserUpdateForbiddenException::class.java)
    }

    @Test
    fun `an administrator should not change the status of their own account`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AccountStatusChange(administrator.id, administrator.id, AccountStatus.DISABLED)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidStatusChangeException::class.java)
    }

    @Test
    fun `an administrator should not set the account back to the verification of the email`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AccountStatusChange(administrator.id, jane.id, AccountStatus.PENDING_VERIFICATION)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidStatusChangeException::class.java)
        assertThat(stored().status).isEqualTo(AccountStatus.ACTIVE)
    }

    @Test
    fun `the status of an unknown account should not change`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AccountStatusChange(administrator.id, UserId.ValidUserId("unknown"), AccountStatus.SUSPENDED)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
