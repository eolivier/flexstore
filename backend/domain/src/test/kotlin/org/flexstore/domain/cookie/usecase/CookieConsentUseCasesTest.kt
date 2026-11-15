package org.flexstore.domain.cookie.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cookie.*
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.infra.repository.InMemoryCookieConsentRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class CookieConsentUseCasesTest : WithAssertions {

    private val recordedAt = Instant.parse("2026-03-10T10:00:00Z")
    private val userRepository = customersWithIds("customer-1")
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val customer = actorResolutionService.actorOf(ValidUserId("customer-1"))
    private val cookieConsentRepository = InMemoryCookieConsentRepository()
    private val readCookieConsentUseCase = ReadCookieConsentUseCase(cookieConsentRepository)
    private val recordCookieConsentUseCase = RecordCookieConsentUseCase(cookieConsentRepository)
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
        val cookieConsent = cookieConsentUseCases.consentOf(customer)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Undecided)
    }

    @ParameterizedTest
    @EnumSource(CookieChoice::class)
    fun `a customer should record their choice for 6 months`(choice: CookieChoice) {
        // Arrange
        val sixMonthsLater = ConsentExpiry(Instant.parse("2026-09-10T10:00:00Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.record(customer, choice)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Given(choice, sixMonthsLater))
    }

    @Test
    fun `a new choice should replace the previous one`() {
        // Arrange
        cookieConsentUseCases.record(customer, CookieChoice.ACCEPTED)
        // Act
        val cookieConsent = cookieConsentUseCases.record(customer, CookieChoice.REFUSED)
        // Assert
        assertThat(cookieConsent).isInstanceOf(CookieConsent.Given::class.java)
        assertThat((cookieConsent as CookieConsent.Given).choice).isEqualTo(CookieChoice.REFUSED)
    }

    @Test
    fun `a choice should be undecided once 6 months are reached`() {
        // Arrange
        cookieConsentUseCases.record(customer, CookieChoice.ACCEPTED)
        InstantProvider.fixAt(Instant.parse("2026-09-10T10:00:00Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.consentOf(customer)
        // Assert
        assertThat(cookieConsent).isEqualTo(CookieConsent.Undecided)
    }

    @Test
    fun `a choice should still be valid just before 6 months`() {
        // Arrange
        cookieConsentUseCases.record(customer, CookieChoice.ACCEPTED)
        InstantProvider.fixAt(Instant.parse("2026-09-10T09:59:59Z"))
        // Act
        val cookieConsent = cookieConsentUseCases.consentOf(customer)
        // Assert
        assertThat(cookieConsent).isInstanceOf(CookieConsent.Given::class.java)
    }

    @Test
    fun `an unknown user should not record a choice`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.record(unknownUser, CookieChoice.ACCEPTED) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not read a consent`() {
        // Arrange
        val unknownUser = actorResolutionService.actorOf(unknownCustomerId)
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.consentOf(unknownUser) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not record a choice`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.record(blockedCustomer, CookieChoice.ACCEPTED) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not read a consent`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = actorResolutionService.actorOf(blockedCustomerId)
        // Act
        val thrown = catchThrowable { cookieConsentUseCases.consentOf(blockedCustomer) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
