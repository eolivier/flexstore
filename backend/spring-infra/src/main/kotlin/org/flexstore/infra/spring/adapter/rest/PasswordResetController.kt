package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.user.service.PasswordResetService
import org.flexstore.domain.user.model.Email
import org.flexstore.infra.spring.adapter.rest.json.JsonChosenPassword
import org.flexstore.infra.spring.adapter.rest.json.JsonPasswordResetRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// A customer who forgot their password (see SecurityConfig: public)
@RestController
@RequestMapping("/api")
class PasswordResetController(private val passwordResetService: PasswordResetService) {

    @PostMapping("/password-reset-requests")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun requestReset(@RequestBody request: JsonPasswordResetRequest) =
        passwordResetService.requestReset(Email(request.email.trim()))

    @PostMapping("/password-resets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reset(@RequestBody chosenPassword: JsonChosenPassword) =
        passwordResetService.reset(chosenPassword.toChosenPassword())
}
