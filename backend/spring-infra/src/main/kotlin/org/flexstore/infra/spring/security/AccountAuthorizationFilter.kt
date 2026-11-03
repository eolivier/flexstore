package org.flexstore.infra.spring.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.flexstore.domain.user.model.Account
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.model.of
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.usecase.ReadAccountUseCase
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

// A token stays valid until it expires, even after its account is deleted, suspended, locked or disabled. This
// filter runs before the authorization of the request: the token of such an account is replaced by an anonymous
// authentication. A public route then answers as it does for a visitor, and a route that requires a signed-in
// user answers 401. Only the requests that carry a token read the account.
class AccountAuthorizationFilter(private val readAccountUseCase: ReadAccountUseCase) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken && accountCannotAct(authentication.token.subject)) {
            SecurityContextHolder.getContext().authentication = anonymous()
        }
        filterChain.doFilter(request, response)
    }

    private fun accountCannotAct(subject: String) = try {
        val account: Account = readAccountUseCase.unfold(UserId.of(subject))
        account.status.refusesSignIn()
    } catch (unknownUser: UserNotFoundException) {
        true
    }

    private fun anonymous() = AnonymousAuthenticationToken(
        ANONYMOUS_KEY, ANONYMOUS_PRINCIPAL, AuthorityUtils.createAuthorityList(ANONYMOUS_ROLE)
    )

    private companion object {
        const val ANONYMOUS_KEY = "anonymous"
        const val ANONYMOUS_PRINCIPAL = "anonymousUser"
        const val ANONYMOUS_ROLE = "ROLE_ANONYMOUS"
    }
}
