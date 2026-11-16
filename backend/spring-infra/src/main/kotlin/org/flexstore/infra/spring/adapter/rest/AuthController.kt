package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.user.administration.UserService
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.SignInOutcome
import org.flexstore.domain.user.service.SignInService
import org.flexstore.domain.user.service.UserSessionService
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonChangePasswordRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonGoogleLoginRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginResponse
import org.flexstore.infra.spring.adapter.rest.json.JsonRefreshRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonRefreshResponse
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorChallengeVerification
import org.flexstore.infra.spring.security.JwtTokenService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class AuthController(
    private val userService: UserService,
    private val signInService: SignInService,
    private val userSessionService: UserSessionService,
    private val jwtTokenService: JwtTokenService
) {

    @PostMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(@PathVariable id: String, @RequestBody request: JsonChangePasswordRequest) {
        userService.changePassword(request.toChangePasswordRequest(id))
    }

    @PostMapping("/login")
    fun login(@RequestBody jsonLoginRequest: JsonLoginRequest): JsonLoginResponse {
        return when (val outcome = signInService.signIn(jsonLoginRequest.toLoginRequest())) {
            is SignInOutcome.SignedIn -> sessionOf(outcome.user)
            is SignInOutcome.TwoFactorChallengeRequired ->
                JsonLoginResponse.twoFactorChallengeRequired(outcome.challengeId.id.value)
        }
    }

    @PostMapping("/login/google")
    fun loginWithGoogle(@RequestBody jsonGoogleLoginRequest: JsonGoogleLoginRequest): JsonLoginResponse {
        return when (val outcome = signInService.signInWithGoogle(jsonGoogleLoginRequest.toGoogleIdToken())) {
            is SignInOutcome.SignedIn -> sessionOf(outcome.user)
            is SignInOutcome.TwoFactorChallengeRequired ->
                JsonLoginResponse.twoFactorChallengeRequired(outcome.challengeId.id.value)
        }
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody jsonRefreshRequest: JsonRefreshRequest): JsonRefreshResponse {
        val signedInSession = userSessionService.refresh(jsonRefreshRequest.toRefreshToken())
        val token = jwtTokenService.issue(signedInSession.account)
        val refreshSession = signedInSession.refreshSession
        return JsonRefreshResponse(
            token.value, token.expiresAt.toString(), refreshSession.refreshToken.value,
            refreshSession.expiry.value.toString()
        )
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody jsonRefreshRequest: JsonRefreshRequest) {
        userSessionService.close(jsonRefreshRequest.toRefreshToken())
    }

    @PostMapping("/login/two-factor-authentication")
    fun verifyTwoFactorCode(@RequestBody verification: JsonTwoFactorChallengeVerification): JsonLoginResponse {
        val user = signInService.verifyTwoFactorCode(verification.toTwoFactorCodeVerification())
        return sessionOf(user)
    }

    private fun sessionOf(user: User.DefinedUser): JsonLoginResponse {
        val refreshSession = userSessionService.open(user.id)
        val token = jwtTokenService.issue(user.account())
        return JsonLoginResponse.signedIn(
            token.value, token.expiresAt.toString(), refreshSession.refreshToken.value,
            refreshSession.expiry.value.toString(), user.toJsonUser()
        )
    }
}
