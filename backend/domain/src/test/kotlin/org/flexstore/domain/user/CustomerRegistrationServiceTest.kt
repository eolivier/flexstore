package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.email.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class CustomerRegistrationServiceTest : WithAssertions {

    private val now = Instant.parse("2026-10-24T10:00:00Z")
    private val userRepository = InMemoryUserRepository()
    private val emailRepository = InMemoryEmailRepository()
    private val verifyEmailUseCase = VerifyEmailUseCase(userRepository, emailRepository)
    private val service = CustomerRegistrationService(
        RegisterCustomerUseCase(userRepository),
        SendVerificationEmailUseCase(userRepository, emailRepository, VerificationPage("https://shop.example/verify-email")),
        verifyEmailUseCase,
        ReadAccountUseCase(userRepository),
        Clock.fixed(now, ZoneOffset.UTC)
    )
    private val jane = RegisterCustomerUseCaseTest.jane

    private fun stored() = userRepository.findById(jane.userId) as User.DefinedUser

    private fun lastLink() = emailRepository.sentVerificationLinks().last().token

    @Test
    fun `a new customer should receive a verification link valid for 1 hour`() {
        // Act
        val account = service.register(jane)
        // Assert
        assertThat(account.status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
        val email = emailRepository.sentVerificationLinks().single()
        assertThat(email.recipient).isEqualTo(jane.email)
        assertThat(email.customerName).isEqualTo(jane.fullName)
        assertThat(email.expiresAt).isEqualTo(ExpirationDate(now + Duration.ofHours(1)))
        assertThat(email.link().value).startsWith("https://shop.example/verify-email?token=")
    }

    @Test
    fun `opening the link should activate the account`() {
        // Arrange
        service.register(jane)
        // Act
        service.verifyEmail(lastLink())
        // Assert
        assertThat(stored().status).isEqualTo(AccountStatus.ACTIVE)
    }

    @Test
    fun `a link should not be used twice`() {
        // Arrange
        service.register(jane)
        val link = lastLink()
        service.verifyEmail(link)
        // Act
        val thrown = catchThrowable { service.verifyEmail(link) }
        // Assert
        assertThat(thrown).isInstanceOf(VerificationLinkUnknownException::class.java)
    }

    @Test
    fun `an expired link should leave the account waiting for the verification`() {
        // Arrange
        service.register(jane)
        val emailVerificationConfirmation = EmailVerificationConfirmation(lastLink(), now + Duration.ofHours(1))
        // Act
        val thrown = catchThrowable { verifyEmailUseCase.unfold(emailVerificationConfirmation) }
        // Assert
        assertThat(thrown).isInstanceOf(VerificationLinkExpiredException::class.java)
        assertThat(stored().status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
    }

    @Test
    fun `an unknown link should be refused`() {
        // Act
        val thrown = catchThrowable { service.verifyEmail(VerificationToken("not-a-link")) }
        // Assert
        assertThat(thrown).isInstanceOf(VerificationLinkUnknownException::class.java)
    }

    @Test
    fun `a new link should expire the previous one, which tells the customer it has expired`() {
        // Arrange
        service.register(jane)
        val previousLink = lastLink()
        service.sendVerificationEmail(jane.userId)
        // Act
        val thrown = catchThrowable { service.verifyEmail(previousLink) }
        // Assert
        assertThat(emailRepository.sentVerificationLinks()).hasSize(2)
        assertThat(thrown).isInstanceOf(VerificationLinkExpiredException::class.java)
        assertThat(stored().status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
    }

    @Test
    fun `the new link should still activate the account`() {
        // Arrange
        service.register(jane)
        service.sendVerificationEmail(jane.userId)
        // Act
        service.verifyEmail(lastLink())
        // Assert
        assertThat(stored().status).isEqualTo(AccountStatus.ACTIVE)
    }

    @Test
    fun `a verified account should not receive a new link`() {
        // Arrange
        service.register(jane)
        service.verifyEmail(lastLink())
        // Act
        val thrown = catchThrowable { service.sendVerificationEmail(jane.userId) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountAlreadyVerifiedException::class.java)
    }
}
