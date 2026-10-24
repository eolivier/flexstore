package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.user.*
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonAccountRegistration
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// The accounts the customers create for themselves (public, see SecurityConfig)
@Tag(name = "Accounts")
@RestController
@RequestMapping("/api/accounts")
class AccountController(private val userService: UserService) {

    @Operation(summary = "Create the account of a customer, which waits for the verification of their email")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody registration: JsonAccountRegistration): JsonUser =
        userService.registerCustomer(registration.toRegistration(UserId.nextId())).toJsonUser()
}
