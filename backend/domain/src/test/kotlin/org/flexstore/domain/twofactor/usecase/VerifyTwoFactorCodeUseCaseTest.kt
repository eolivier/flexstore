package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.twofactor.model.TwoFactorChallengeNotFoundException
import org.flexstore.domain.twofactor.model.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryTwoFactorChallengeRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Instant

class VerifyTwoFactorCodeUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val twoFactorChallengeRepository = InMemoryTwoFactorChallengeRepository()
    private val useCase = VerifyTwoFactorCodeUseCase(twoFactorChallengeRepository, userRepository)
    private val secret = TwoFactorSecret.next()
    private val administrator = User.DefinedUser(
        UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")),
        Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN,
        twoFactorAuthentication = TwoFactorAuthentication.Enabled(secret)
    )

    init {
        userRepository.save(administrator)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun challengedAt(instant: Instant): TwoFactorChallengeId {
        InstantProvider.fixAt(instant)
        val challenge = TwoFactorChallenge.issuedFor(administrator.id)
        twoFactorChallengeRepository.save(challenge)
        return challenge.id
    }

    @Test
    fun `the right code should authenticate the challenged administrator`() {
        // Arrange
        val challengeId = challengedAt(Instant.now())
        val code = secret.codeAt(Instant.now())
        // Act
        useCase.unfold(TwoFactorCodeVerification(challengeId, code))
        // Assert
        assertThat(useCase.authenticatedUser.id).isEqualTo(administrator.id)
    }

    @Test
    fun `a used challenge should not be verified again`() {
        // Arrange
        val challengeId = challengedAt(Instant.now())
        useCase.unfold(TwoFactorCodeVerification(challengeId, secret.codeAt(Instant.now())))
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorCodeVerification(challengeId, secret.codeAt(Instant.now())))
        }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorChallengeNotFoundException::class.java)
    }

    @Test
    fun `a wrong code should not authenticate`() {
        // Arrange
        val challengeId = challengedAt(Instant.now())
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorCodeVerification(challengeId, TwoFactorCode("000000")))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(TwoFactorCodeInvalidException::class.java)
    }

    @Test
    fun `an unknown challenge should not authenticate`() {
        // Act
        val unknownId = TwoFactorChallengeId.nextId()
        val verification = TwoFactorCodeVerification(unknownId, TwoFactorCode("123456"))
        val thrown = catchThrowable { useCase.unfold(verification) }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorChallengeNotFoundException::class.java)
    }

    @Test
    fun `a challenge older than its validity should not authenticate`() {
        // Arrange
        val issuedAt = Instant.parse("2026-01-01T10:00:00Z")
        val challengeId = challengedAt(issuedAt)
        InstantProvider.fixAt(issuedAt.plus(TwoFactorChallenge.VALIDITY).plusSeconds(1))
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorCodeVerification(challengeId, secret.codeAt(issuedAt)))
        }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorChallengeNotFoundException::class.java)
    }
}
