package org.flexstore.infra.spring.security

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.usecase.ReadAccountUseCase
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant

class AccountAuthorizationFilterTest {

    private val userRepository = InMemoryUserRepository()
    private val filter = AccountAuthorizationFilter(ReadAccountUseCase(userRepository))
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()
    private val filterChain = MockFilterChain()

    private fun customerWith(status: AccountStatus) = DefinedUser(
        UserId.ValidUserId("customer-1"), FullName(FirstName("Jane"), LastName("Doe")),
        Email("jane@flexstore.fr"), HashedPassword("hashed"), Role.USER, status
    )

    private fun tokenOf(subject: String): JwtAuthenticationToken {
        val jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(subject)
            .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build()
        return JwtAuthenticationToken(jwt)
    }

    private fun currentAuthentication() = SecurityContextHolder.getContext().authentication

    @BeforeEach
    fun signIn() {
        SecurityContextHolder.getContext().authentication = tokenOf("customer-1")
    }

    @AfterEach
    fun signOut() {
        SecurityContextHolder.clearContext()
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["ACTIVE", "PENDING_VERIFICATION"])
    fun `the token of an account that can sign in should be kept`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWith(status))
        // Act
        filter.doFilter(request, response, filterChain)
        // Assert
        assertThat(currentAuthentication()).isInstanceOf(JwtAuthenticationToken::class.java)
        assertThat(filterChain.request).isNotNull
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `the token of a suspended, locked or disabled account should become anonymous`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWith(status))
        // Act
        filter.doFilter(request, response, filterChain)
        // Assert
        assertThat(currentAuthentication()).isInstanceOf(AnonymousAuthenticationToken::class.java)
        assertThat(filterChain.request).isNotNull
    }

    @Test
    fun `the token of an unknown user should become anonymous`() {
        // Act
        filter.doFilter(request, response, filterChain)
        // Assert
        assertThat(currentAuthentication()).isInstanceOf(AnonymousAuthenticationToken::class.java)
        assertThat(filterChain.request).isNotNull
    }

    @Test
    fun `a request without a token should go on untouched`() {
        // Arrange
        SecurityContextHolder.clearContext()
        // Act
        filter.doFilter(request, response, filterChain)
        // Assert
        assertThat(currentAuthentication()).isNull()
        assertThat(filterChain.request).isNotNull
    }
}
