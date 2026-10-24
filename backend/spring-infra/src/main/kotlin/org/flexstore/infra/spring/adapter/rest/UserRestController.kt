package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.mapping.toJsonUsers
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonChangePasswordRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginRequest
import org.flexstore.infra.spring.adapter.rest.json.JsonLoginResponse
import org.flexstore.infra.spring.security.JwtTokenService
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class UserRestController(val userService: UserService, private val jwtTokenService: JwtTokenService) {

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created",
        content = [Content(schema = Schema(implementation = DefinedJsonUser::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody draftJsonUser: DraftJsonUser): JsonUser {
        return userService.createUser(draftJsonUser.toUser()).toJsonUser()
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
    fun getAllUsers(): List<JsonUser> = userService.readAllUsers().toJsonUsers()

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
    fun getUser(@PathVariable id: String): JsonUser {
        val userId = ValidUserId(id)
        return userService.readUser(userId).toJsonUser()
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
    fun updateUser(@PathVariable id: String, @RequestBody jsonUser: DefinedJsonUser) {
        userService.updateUser(jsonUser.toUser(id))
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
    fun deleteUser(@PathVariable id: String) {
        val userId = ValidUserId(id)
        userService.deleteUser(userId)
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
                description = "Successfully authenticated: the token is returned with the user",
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
        val user = userService.login(jsonLoginRequest.toLoginRequest()) as User.DefinedUser
        val token = jwtTokenService.issue(user)
        return JsonLoginResponse(token.value, token.expiresAt.toString(), user.toJsonUser())
    }
}