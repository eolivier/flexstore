package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class AuthController(
    private val userService: UserService,
    private val signInService: SignInService,
    private val userSessionService: UserSessionService,
    private val jwtTokenService: JwtTokenService
) {

    @Operation(summary = "Change the password of a user",
        responses = [
            ApiResponse(responseCode = "204", description = "Password changed"),
            ApiResponse(responseCode = "400", description = "Wrong current password"),
            ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            ApiResponse(responseCode = "403", description = "Not the password of the signed-in user"),
            ApiResponse(responseCode = "422", description = "New password too short")
        ]
    )
    @PostMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(@PathVariable id: String, @RequestBody request: JsonChangePasswordRequest) {
        userService.changePassword(request.toChangePasswordRequest(id))
    }

    @Operation(summary = "Login with email and password",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "SIGNED_IN: the token is returned with the user; " +
                    "TWO_FACTOR_REQUIRED: the code of the authenticator app is still needed, " +
                    "see POST /api/users/login/two-factor-authentication",
                content = [
                    Content(schema = Schema(implementation = JsonLoginResponse::class))
                ]
            ),
            ApiResponse(responseCode = "401", description = "Invalid credentials")
        ]
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody jsonLoginRequest: JsonLoginRequest): JsonLoginResponse {
        return when (val outcome = signInService.signIn(jsonLoginRequest.toLoginRequest())) {
            is SignInOutcome.SignedIn -> sessionOf(outcome.user)
            is SignInOutcome.TwoFactorChallengeRequired ->
                JsonLoginResponse.twoFactorChallengeRequired(outcome.challengeId.value)
        }
    }

    @Operation(summary = "Login with a Google account",
        description = "The ID token that the Sign in with Google button gives to the browser. The account " +
            "that has the email of the Google account is signed in; a customer who has none gets one.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "SIGNED_IN: the token is returned with the user; TWO_FACTOR_REQUIRED: the " +
                    "code of the authenticator app is still needed, see " +
                    "POST /api/users/login/two-factor-authentication",
                content = [
                    Content(schema = Schema(implementation = JsonLoginResponse::class))
                ]
            ),
            ApiResponse(
                responseCode = "401",
                description = "The Google token is refused, or Sign in with Google is not set up"
            ),
            ApiResponse(responseCode = "403", description = "The account is suspended, locked or disabled")
        ]
    )
    @PostMapping("/login/google")
    @ResponseStatus(HttpStatus.OK)
    fun loginWithGoogle(@RequestBody jsonGoogleLoginRequest: JsonGoogleLoginRequest): JsonLoginResponse {
        return when (val outcome = signInService.signInWithGoogle(jsonGoogleLoginRequest.toGoogleIdToken())) {
            is SignInOutcome.SignedIn -> sessionOf(outcome.user)
            is SignInOutcome.TwoFactorChallengeRequired ->
                JsonLoginResponse.twoFactorChallengeRequired(outcome.challengeId.value)
        }
    }

    @Operation(summary = "Renew the access of a signed-in user with their refresh token",
        description = "The refresh token works once: the response holds a new token and a new refresh token. " +
            "A refresh token that was already used ends all the sessions of its user.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "A new token and the refresh token that replaces the one presented",
                content = [Content(schema = Schema(implementation = JsonRefreshResponse::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unknown, expired or already used refresh token"),
            ApiResponse(responseCode = "403", description = "The account is suspended, locked or disabled")
        ]
    )
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(@RequestBody jsonRefreshRequest: JsonRefreshRequest): JsonRefreshResponse {
        val signedInSession = userSessionService.refresh(jsonRefreshRequest.toRefreshToken())
        val token = jwtTokenService.issue(signedInSession.account)
        val refreshSession = signedInSession.refreshSession
        return JsonRefreshResponse(
            token.value, token.expiresAt.toString(), refreshSession.refreshToken.value,
            refreshSession.expiry.value.toString()
        )
    }

    @Operation(summary = "Sign out: end the session of a refresh token",
        description = "Signing out twice, or with an unknown refresh token, is no error.",
        responses = [ApiResponse(responseCode = "204", description = "The session is ended")]
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody jsonRefreshRequest: JsonRefreshRequest) {
        userSessionService.close(jsonRefreshRequest.toRefreshToken())
    }

    @Operation(summary = "Complete a challenged sign-in with the code of the authenticator app",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully authenticated: the token and the user"
            ),
            ApiResponse(responseCode = "401", description = "Wrong code, or unknown or expired challenge")
        ]
    )
    @PostMapping("/login/two-factor-authentication")
    @ResponseStatus(HttpStatus.OK)
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
