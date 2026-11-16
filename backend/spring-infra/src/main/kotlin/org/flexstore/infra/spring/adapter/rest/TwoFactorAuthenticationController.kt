package org.flexstore.infra.spring.adapter.rest

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

@RestController
@RequestMapping("/api/users")
class TwoFactorAuthenticationController(
    private val userService: UserService,
    private val twoFactorAuthenticationService: TwoFactorAuthenticationService,
    private val actorResolutionService: ActorResolutionService
) {

    @PostMapping("/{id}/two-factor-authentication/enrollment")
    fun startTwoFactorEnrollment(@PathVariable id: String): JsonTwoFactorEnrollment {
        val customer = actorResolutionService.actorOf(UserId.of(id))
        return JsonTwoFactorEnrollment.of(twoFactorAuthenticationService.startEnrollment(customer))
    }

    @PostMapping("/{id}/two-factor-authentication/confirmation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun confirmTwoFactorEnrollment(@PathVariable id: String, @RequestBody code: JsonTwoFactorCode) {
        val customer = actorResolutionService.actorOf(UserId.of(id))
        twoFactorAuthenticationService.confirmEnrollment(customer, code.toTwoFactorCode())
    }

    @DeleteMapping("/{id}/two-factor-authentication")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun disableTwoFactorAuthentication(
        @PathVariable id: String,
        @RequestBody request: JsonTwoFactorDisableRequest
    ) {
        val customer = actorResolutionService.actorOf(UserId.of(id))
        twoFactorAuthenticationService.disable(customer, request.toPlainPassword())
    }

    @PutMapping("/{id}/two-factor-authentication")
    fun adminDisableTwoFactorAuthentication(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String
    ): JsonUser {
        val actor = actorResolutionService.actorOf(editor)
        twoFactorAuthenticationService.disableForUser(actor, UserId.of(id))
        return userService.readUser(actor, UserId.of(id)).toJsonUser()
    }
}
