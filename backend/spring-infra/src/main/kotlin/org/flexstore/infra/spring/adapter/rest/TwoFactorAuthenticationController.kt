package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.twofactor.TwoFactorAuthenticationService
import org.flexstore.domain.user.administration.UserService
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorCode
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorDisableRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonTwoFactorEnrollment
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class TwoFactorAuthenticationController(
    private val userService: UserService,
    private val twoFactorAuthenticationService: TwoFactorAuthenticationService,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Start the enrollment of two-factor authentication",
        responses = [
            ApiResponse(responseCode = "200", description = "The secret and the link of the QR code"),
            ApiResponse(responseCode = "403", description = "Not the signed-in user")
        ]
    )
    @PostMapping("/{id}/two-factor-authentication/enrollment")
    fun startTwoFactorEnrollment(@PathVariable id: String): JsonTwoFactorEnrollment {
        val customer = actorResolutionService.actorOf(UserId.of(id))
        return JsonTwoFactorEnrollment.of(twoFactorAuthenticationService.startEnrollment(customer))
    }

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
        val customer = actorResolutionService.actorOf(UserId.of(id))
        twoFactorAuthenticationService.confirmEnrollment(customer, code.toTwoFactorCode())
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
        val customer = actorResolutionService.actorOf(UserId.of(id))
        twoFactorAuthenticationService.disable(customer, request.toPlainPassword())
    }

    @Operation(
        summary = "Disable the two-factor authentication of another user (administrator only)",
        description = "Reserved to an administrator, on another account than their own. " +
            "No current password is asked, unlike the self-service operation.",
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
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        twoFactorAuthenticationService.disableForUser(actor, UserId.of(id))
        return userService.readUser(actor, UserId.of(id)).toJsonUser()
    }
}
