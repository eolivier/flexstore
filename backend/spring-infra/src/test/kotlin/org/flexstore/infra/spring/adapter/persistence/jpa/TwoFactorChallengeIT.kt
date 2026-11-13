package org.flexstore.infra.spring.adapter.persistence.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant

@ActiveProfiles("test")
@SpringBootTest
class TwoFactorChallengeIT {

    @Autowired
    private lateinit var challengeRepository: TwoFactorChallengeRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jpaUserRepository: JpaUserRepository

    private val jane = User.DefinedUser(
        UserId.ValidUserId("challenge-it-jane"),
        FullName(FirstName("Jane"), LastName("Doe")),
        Email("challenge-it.jane@example.com"),
        PlainPassword("password123")
    )
    private val challenge = TwoFactorChallenge(
        TwoFactorChallengeId("challenge-it-1"),
        jane.id,
        Instant.parse("2026-11-13T10:00:00Z")
    )

    @BeforeEach
    fun createJane() {
        userRepository.save(jane)
    }

    @AfterEach
    fun deleteJane() {
        jpaUserRepository.deleteById(jane.id.value)
    }

    @Test
    fun `a saved challenge should be found with its user and its issue moment`() {
        // Arrange
        challengeRepository.save(challenge)
        // Act
        val lookup = challengeRepository.find(challenge.id)
        // Assert
        assertThat(lookup).isEqualTo(TwoFactorChallengeLookup.Found(challenge))
    }

    @Test
    fun `an unknown challenge should not be found`() {
        // Act
        val lookup = challengeRepository.find(TwoFactorChallengeId("challenge-it-unknown"))
        // Assert
        assertThat(lookup).isEqualTo(TwoFactorChallengeLookup.NotFound)
    }

    @Test
    fun `a deleted challenge should not be found any more`() {
        // Arrange
        challengeRepository.save(challenge)
        // Act
        challengeRepository.delete(challenge.id)
        // Assert
        assertThat(challengeRepository.find(challenge.id)).isEqualTo(TwoFactorChallengeLookup.NotFound)
    }

    @Test
    fun `deleting a challenge that does not exist should change nothing`() {
        // Arrange
        challengeRepository.save(challenge)
        // Act
        challengeRepository.delete(TwoFactorChallengeId("challenge-it-unknown"))
        // Assert
        assertThat(challengeRepository.find(challenge.id)).isEqualTo(TwoFactorChallengeLookup.Found(challenge))
    }

    @Test
    fun `the challenges of a user should be deleted with the user`() {
        // Arrange
        challengeRepository.save(challenge)
        // Act
        jpaUserRepository.deleteById(jane.id.value)
        // Assert
        assertThat(challengeRepository.find(challenge.id)).isEqualTo(TwoFactorChallengeLookup.NotFound)
    }
}
