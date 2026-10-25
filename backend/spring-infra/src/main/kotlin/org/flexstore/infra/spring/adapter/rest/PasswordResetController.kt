package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.user.PasswordResetService
import org.flexstore.domain.user.model.Email
import org.flexstore.infra.spring.adapter.rest.json.JsonChosenPassword
import org.flexstore.infra.spring.adapter.rest.json.JsonPasswordResetRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// A customer who forgot their password (see SecurityConfig: public)
@Tag(name = "Accounts")
@RestController
@RequestMapping("/api")
class PasswordResetController(private val passwordResetService: PasswordResetService) {

    @Operation(summary = "Send a link to choose a new password (valid for 1 hour), if the email has an account")
    @PostMapping("/password-reset-requests")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun requestReset(@RequestBody request: JsonPasswordResetRequest) =
        passwordResetService.requestReset(Email(request.email.trim()))

    @Operation(summary = "Choose a new password with the token of the link; the link cannot be used again")
    @PostMapping("/password-resets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reset(@RequestBody chosenPassword: JsonChosenPassword) =
        passwordResetService.reset(chosenPassword.toChosenPassword())
}
