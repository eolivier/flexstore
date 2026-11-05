package org.flexstore.domain.cookie.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cookie.*
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryCookieConsentRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant
import org.flexstore.domain.user.administration.unknownUser

class CookieConsentUseCasesTest : WithAssertions {

    private val recordedAt = Instant.parse("2026-03-10T10:00:00Z")
    private val customer = DefinedUser(
        ValidUserId("customer-1"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane@flexstore.fr"),
        HashedPassword("hashed"), Role.USER
    )
    private val userRepository = InMemoryUserRepository().apply { save(customer) }
    private val cookieConsentRepository = InMemoryCookieConsentRepository()
    private val readCookieConsentUseCase = ReadCookieConsentUseCase(cookieConsentRepository, userRepository)
    private val recordCookieConsentUseCase =
        RecordCookieConsentUseCase(cookieConsentRepository, userRepository)
    private val cookieConsentUseCases =
        CookieConsentUseCases(readCookieConsentUseCase, recordCookieConsentUseCase)

    @BeforeEach
    fun fixTheMoment() {
        InstantProvider.fixAt(recordedAt)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    @Test
    fun `a customer who never chose should be undecided`() {
        // Act
        val cookieConsent = cookieConsentUseCases.consentOf(customer.id)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Undecided)
    }

    @ParameterizedTest
    @EnumSource(CookieChoice::class)
    fun `a customer should record their choice for 6 months`(choice: CookieChoice) {
        // Arrange
        val sixMonthsLater = ConsentExpiry(Instant.parse("2026-09-10T10:00:00Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.record(customer.id, choice)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Given(choice, sixMonthsLater))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        cookieConsentUseCases.record(customer.id, CookieChoice.ACCEPTED)
        // Act
        val cookieConsent = cookieConsentUseCases.record(customer.id, CookieChoice.REFUSED)
        // Assert
        assertThat(cookieConsent).isInstanceOf(CookieConsent.Given::class.java)
        assertThat((cookieConsent as CookieConsent.Given).choice).isEqualTo(CookieChoice.REFUSED)
    }

    @Test
    fun `a choice should be undecided once 6 months are reached`() {
        // Arrange
        cookieConsentUseCases.record(customer.id, CookieChoice.ACCEPTED)
        InstantProvider.fixAt(Instant.parse("2026-09-10T10:00:00Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.consentOf(customer.id)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Undecided)
    }

    @Test
    fun `a choice should still be valid just before 6 months`() {
        // Arrange
        cookieConsentUseCases.record(customer.id, CookieChoice.ACCEPTED)
        InstantProvider.fixAt(Instant.parse("2026-09-10T09:59:59Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.consentOf(customer.id)
        // Assert
        assertThat(cookieConsent).isInstanceOf(CookieConsent.Given::class.java)
    }

    @Test
    fun `an unknown user should not record a choice`() {
        // Arrange
        val unknownUser = ValidUserId("unknown")
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.record(unknownUser, CookieChoice.ACCEPTED) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read a consent`() {
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.consentOf(ValidUserId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }
}
