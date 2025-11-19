package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.service.UserService
import org.flexstore.infra.spring.adapter.mapping.toJsonUsers
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class UserRestController(val userService: UserService) {

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created",
        content = [Content(schema = Schema(implementation = JsonUser::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody jsonUser: JsonUser): User {
        return userService.createUser(jsonUser.toUser())
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
                        schema = Schema(implementation = JsonUser::class)
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
                    Content(schema = Schema(implementation = JsonUser::class))
                ]
            )
        ]
    )
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): User {
        val userId = ValidUserId(id)
        return userService.readUser(userId)
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
    fun updateUser(@PathVariable id: String, @RequestBody jsonUser: JsonUser) {
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

    @Operation(summary = "Login with email and password",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully authenticated",
                content = [
                    Content(schema = Schema(implementation = JsonUser::class))
                ]
            )
        ]
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    fun login(@RequestBody jsonLoginRequest: org.flexstore.infra.spring.adapter.rest.json.JsonLoginRequest): User {
        return userService.login(jsonLoginRequest.toLoginRequest())
    }
}