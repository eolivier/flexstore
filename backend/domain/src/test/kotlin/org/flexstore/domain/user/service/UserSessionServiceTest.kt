package org.flexstore.domain.user.service

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.domain.user.usecase.*
import org.flexstore.infra.repository.InMemoryRefreshSessionRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Duration
import java.time.Instant

class UserSessionServiceTest : WithAssertions {

    private val signedInAt = Instant.parse("2026-11-03T10:00:00Z")
    private val janeId = ValidUserId("jane")
    private val johnId = ValidUserId("john")
    private val userRepository = InMemoryUserRepository().apply {
        save(userOf(janeId, AccountStatus.ACTIVE))
        save(userOf(johnId, AccountStatus.ACTIVE))
    }
    private val sessionRepository = InMemoryRefreshSessionRepository()
    private val service = UserSessionService(
        OpenSessionUseCase(sessionRepository, userRepository),
        ReadRefreshSessionUseCase(sessionRepository),
        RotateRefreshSessionUseCase(sessionRepository, userRepository),
        RevokeRefreshSessionUseCase(sessionRepository),
        RevokeAllRefreshSessionsUseCase(sessionRepository),
        ReadAccountUseCase(userRepository)
    )

    private fun userOf(id: ValidUserId, status: AccountStatus) = DefinedUser(
        id, FullName(FirstName("Jane"), LastName("Doe")), Email("${id.value}@flexstore.fr"),
        HashedPassword("hashed"), Role.USER, status
    )

    @BeforeEach
    fun fixTheMoment() {
        InstantProvider.fixAt(signedInAt)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    @Test
    fun `a session should last 7 days`() {
        // Act
        val session = service.open(janeId)
        // Assert
        assertThat(session.expiry.value).isEqualTo(signedInAt.plus(Duration.ofDays(7)))
        assertThat(session.isOpen()).isTrue()
    }

    @Test
    fun `an unknown user should not open a session`() {
        // Act
        val thrown = catchThrowable { service.open(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `a refresh token should be replaced by another one of the same user`() {
        // Arrange
        val session = service.open(janeId)
        // Act
        val signedInSession = service.refresh(session.refreshToken)
        // Assert
        assertThat(signedInSession.account.id).isEqualTo(janeId)
        assertThat(signedInSession.refreshSession.refreshToken).isNotEqualTo(session.refreshToken)
        assertThat(sessionRepository.get(session.refreshToken).isRevoked()).isTrue()
        assertThat(sessionRepository.get(signedInSession.refreshSession.refreshToken).isOpen()).isTrue()
    }

    @Test
    fun `the new refresh token should work in its turn`() {
        // Arrange
        val first = service.refresh(service.open(janeId).refreshToken)
        // Act
        val second = service.refresh(first.refreshSession.refreshToken)
        // Assert
        assertThat(second.account.id).isEqualTo(janeId)
    }

    @Test
    fun `a refresh token already used should be refused`() {
        // Arrange
        val session = service.open(janeId)
        service.refresh(session.refreshToken)
        // Act
        val thrown = catchThrowable { service.refresh(session.refreshToken) }
        // Assert
        assertThat(thrown).isInstanceOf(RefreshTokenReusedException::class.java)
    }

    @Test
    fun `a refresh token already used should end all the sessions of its user`() {
        // Arrange
        val stolen = service.open(janeId)
        val otherDevice = service.open(janeId)
        val renewed = service.refresh(stolen.refreshToken)
        // Act
        catchThrowable { service.refresh(stolen.refreshToken) }
        // Assert
        assertThat(sessionRepository.hasOpenSessionOf(janeId)).isFalse()
        assertThat(catchThrowable { service.refresh(otherDevice.refreshToken) })
            .isInstanceOf(RefreshTokenReusedException::class.java)
        assertThat(catchThrowable { service.refresh(renewed.refreshSession.refreshToken) })
            .isInstanceOf(RefreshTokenReusedException::class.java)
    }

    @Test
    fun `a refresh token already used should keep the sessions of the other users`() {
        // Arrange
        val jane = service.open(janeId)
        val john = service.open(johnId)
        service.refresh(jane.refreshToken)
        // Act
        catchThrowable { service.refresh(jane.refreshToken) }
        // Assert
        assertThat(sessionRepository.hasOpenSessionOf(johnId)).isTrue()
        assertThat(service.refresh(john.refreshToken).account.id).isEqualTo(johnId)
    }

    @Test
    fun `an unknown refresh token should be refused`() {
        // Act
        val thrown = catchThrowable { service.refresh(RefreshToken("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(RefreshTokenUnknownException::class.java)
    }

    @Test
    fun `a refresh token should be refused once its 7 days are reached`() {
        // Arrange
        val session = service.open(janeId)
        InstantProvider.fixAt(session.expiry.value)
        // Act
        val thrown = catchThrowable { service.refresh(session.refreshToken) }
        // Assert
        assertThat(thrown).isInstanceOf(RefreshTokenExpiredException::class.java)
    }

    @Test
    fun `a refresh token should work just before its 7 days are reached`() {
        // Arrange
        val session = service.open(janeId)
        InstantProvider.fixAt(session.expiry.value.minusSeconds(1))
        // Act
        val signedInSession = service.refresh(session.refreshToken)
        // Assert
        assertThat(signedInSession.account.id).isEqualTo(janeId)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a refresh token of a suspended, locked or disabled account should be refused`(status: AccountStatus) {
        // Arrange
        val session = service.open(janeId)
        userRepository.save(userOf(janeId, status))
        // Act
        val thrown = catchThrowable { service.refresh(session.refreshToken) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountSignInRefusedException::class.java)
    }

    @Test
    fun `a refresh token should work again once its account is reactivated`() {
        // Arrange
        val session = service.open(janeId)
        userRepository.save(userOf(janeId, AccountStatus.SUSPENDED))
        catchThrowable { service.refresh(session.refreshToken) }
        userRepository.save(userOf(janeId, AccountStatus.ACTIVE))
        // Act
        val signedInSession = service.refresh(session.refreshToken)
        // Assert
        assertThat(signedInSession.account.id).isEqualTo(janeId)
    }

    @Test
    fun `a refresh token of a customer who has not verified their email should work`() {
        // Arrange
        val session = service.open(janeId)
        userRepository.save(userOf(janeId, AccountStatus.PENDING_VERIFICATION))
        // Act
        val signedInSession = service.refresh(session.refreshToken)
        // Assert
        assertThat(signedInSession.account.id).isEqualTo(janeId)
    }

    @Test
    fun `a refresh token of a deleted user should be refused`() {
        // Arrange
        val session = service.open(janeId)
        userRepository.delete(janeId)
        // Act
        val thrown = catchThrowable { service.refresh(session.refreshToken) }
        // Assert
        assertThat(thrown).isInstanceOf(RefreshTokenUnknownException::class.java)
    }

    @Test
    fun `signing out should end the session`() {
        // Arrange
        val session = service.open(janeId)
        // Act
        service.close(session.refreshToken)
        // Assert
        assertThat(sessionRepository.get(session.refreshToken).isRevoked()).isTrue()
        assertThat(catchThrowable { service.refresh(session.refreshToken) })
            .isInstanceOf(RefreshTokenReusedException::class.java)
    }

    @Test
    fun `signing out should keep the other sessions of the user`() {
        // Arrange
        val session = service.open(janeId)
        val otherDevice = service.open(janeId)
        // Act
        service.close(session.refreshToken)
        // Assert
        assertThat(service.refresh(otherDevice.refreshToken).account.id).isEqualTo(janeId)
    }

    @Test
    fun `signing out twice should be no error`() {
        // Arrange
        val session = service.open(janeId)
        service.close(session.refreshToken)
        // Act
        val thrown = catchThrowable { service.close(session.refreshToken) }
        // Assert
        assertThat(thrown).isNull()
    }

    @Test
    fun `signing out with an unknown refresh token should be no error`() {
        // Act
        val thrown = catchThrowable { service.close(RefreshToken("unknown")) }
        // Assert
        assertThat(thrown).isNull()
    }
}
