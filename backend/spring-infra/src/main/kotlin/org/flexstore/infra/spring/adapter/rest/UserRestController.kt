package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.twofactor.TwoFactorAuthenticationService
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.domain.user.service.*
import org.flexstore.domain.user.usecase.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.mapping.toJsonUsers
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonChangePasswordRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonGoogleLoginRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginResponse
import org.flexstore.infra.spring.adapter.rest.json.JsonRefreshRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonRefreshResponse
import org.flexstore.infra.spring.adapter.rest.json.JsonAccountStatus
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorChallengeVerification
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorCode
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorDisableRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorEnrollment
import org.flexstore.infra.spring.security.JwtTokenService
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.flexstore.domain.sharedkernel.PageNumber
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.administration.UserService
import org.flexstore.domain.user.administration.usecase.AccountStatusChange
import org.flexstore.domain.user.administration.usecase.CreateUserRequest
import org.flexstore.domain.user.administration.usecase.UpdateUserRequest

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class UserRestController(
    val userService: UserService,
    private val signInService: SignInService,
    private val userSessionService: UserSessionService,
    private val jwtTokenService: JwtTokenService,
    private val twoFactorAuthenticationService: TwoFactorAuthenticationService
) {

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created",
        content = [Content(schema = Schema(implementation = DefinedJsonUser::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@AuthenticationPrincipal creator: Jwt, @RequestBody draftJsonUser: DraftJsonUser): JsonUser {
        val createUserRequest = CreateUserRequest(UserId.of(creator.subject), draftJsonUser.toUser())
        return userService.createUser(createUserRequest).toJsonUser()
    }
    @Operation(
        summary = "Retrieve all users",
        description = "Returns the full list of users",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of users",
                content = [
                    Content(
                        schema = Schema(implementation = DefinedJsonUser::class)
                    )
                ]
            )
        ]
    )
    @GetMapping
    fun getAllUsers(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int
    ): ResponseEntity<List<JsonUser>> {
        val users = userService.readAllUsers(UserId.of(reader.subject), PageRequest.of(page, size))
        return pagedBody(users.content.asList().toJsonUsers(), users.total)
    }

    @Operation(summary = "Retrieve a user by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the user",
                content = [
                    Content(schema = Schema(implementation = DefinedJsonUser::class))
                ]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getUser(@AuthenticationPrincipal reader: Jwt, @PathVariable id: String): JsonUser {
        val userId = ValidUserId(id)
        return userService.readUser(UserId.of(reader.subject), userId).toJsonUser()
    }

    @Operation(summary = "Update a user by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated the user"
            )
        ]
    )
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateUser(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody editedUser: EditedJsonUser
    ): JsonUser {
        val editorId = UserId.of(editor.subject)
        userService.updateUser(UpdateUserRequest(editorId, editedUser.toUser(id)))
        return userService.readUser(editorId, UserId.of(id)).toJsonUser()
    }

    @Operation(summary = "Change the status of an account: ACTIVE, SUSPENDED, LOCKED or DISABLED (administrator only)")
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    fun changeAccountStatus(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody status: JsonAccountStatus
    ): JsonUser {
        val editorId = UserId.of(editor.subject)
        val accountStatusChange = AccountStatusChange(editorId, UserId.of(id), status.toAccountStatus())
        return userService.changeAccountStatus(accountStatusChange).toJsonUser()
    }

    @Operation(summary = "Delete a user by ID",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Successfully deleted the user"
            )
        ]
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@AuthenticationPrincipal editor: Jwt, @PathVariable id: String) {
        val userId = ValidUserId(id)
        userService.deleteUser(UserId.of(editor.subject), userId)
    }

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
                description = "SIGNED_IN: the token is returned with the user; TWO_FACTOR_REQUIRED: the code of " +
                    "the authenticator app is still needed, see POST /api/users/login/two-factor-authentication",
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
            ApiResponse(responseCode = "200", description = "Successfully authenticated: the token and the user"),
            ApiResponse(responseCode = "401", description = "Wrong code, or unknown or expired challenge")
        ]
    )
    @PostMapping("/login/two-factor-authentication")
    @ResponseStatus(HttpStatus.OK)
    fun verifyTwoFactorCode(@RequestBody verification: JsonTwoFactorChallengeVerification): JsonLoginResponse {
        val user = signInService.verifyTwoFactorCode(verification.toTwoFactorCodeVerification())
        return sessionOf(user)
    }

    @Operation(summary = "Start the enrollment of two-factor authentication",
        responses = [
            ApiResponse(responseCode = "200", description = "The secret and the link of the QR code"),
            ApiResponse(responseCode = "403", description = "Not the signed-in user")
        ]
    )
    @PostMapping("/{id}/two-factor-authentication/enrollment")
    fun startTwoFactorEnrollment(@PathVariable id: String): JsonTwoFactorEnrollment =
        JsonTwoFactorEnrollment.of(twoFactorAuthenticationService.startEnrollment(UserId.of(id)))

    @Operation(summary = "Confirm the enrollment with the first code of the authenticator app",
        responses = [
            ApiResponse(responseCode = "204", description = "Two-factor authentication is now enabled"),
            ApiResponse(responseCode = "401", description = "Wrong code"),
            ApiResponse(responseCode = "403", description = "Not the signed-in user"),
            ApiResponse(responseCode = "409", description = "No enrollment is pending")
        ]
    )
    @PostMapping("/{id}/two-factor-authentication/confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun confirmTwoFactorEnrollment(@PathVariable id: String, @RequestBody code: JsonTwoFactorCode) {
        twoFactorAuthenticationService.confirmEnrollment(UserId.of(id), code.toTwoFactorCode())
    }

    @Operation(summary = "Disable two-factor authentication, confirmed with the current password",
        responses = [
            ApiResponse(responseCode = "204", description = "Two-factor authentication is now disabled"),
            ApiResponse(responseCode = "400", description = "Wrong current password"),
            ApiResponse(responseCode = "403", description = "Not the signed-in user"),
            ApiResponse(responseCode = "409", description = "Two-factor authentication is not enabled")
        ]
    )
    @DeleteMapping("/{id}/two-factor-authentication")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disableTwoFactorAuthentication(
        @PathVariable id: String,
        @RequestBody request: JsonTwoFactorDisableRequest
    ) {
        twoFactorAuthenticationService.disable(UserId.of(id), request.toPlainPassword())
    }

    @Operation(
        summary = "Disable the two-factor authentication of another user (administrator only)",
        description = "Reserved to an administrator, on another account than their own. No current password is " +
            "asked, unlike the self-service operation.",
        responses = [
            ApiResponse(responseCode = "200", description = "The user with two-factor authentication disabled"),
            ApiResponse(responseCode = "403", description = "Not an administrator"),
            ApiResponse(responseCode = "409", description = "Two-factor authentication is not enabled"),
            ApiResponse(responseCode = "422", description = "Own account, or unknown user")
        ]
    )
    @PutMapping("/{id}/two-factor-authentication")
    @ResponseStatus(HttpStatus.OK)
    fun adminDisableTwoFactorAuthentication(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String
    ): JsonUser {
        val editorId = UserId.of(editor.subject)
        twoFactorAuthenticationService.disableForUser(editorId, UserId.of(id))
        return userService.readUser(editorId, UserId.of(id)).toJsonUser()
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