package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.sharedkernel.PageNumber
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.PageSize
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.domain.user.model.of
import org.flexstore.domain.user.administration.UserService
import org.flexstore.domain.user.administration.usecase.AccountStatusChange
import org.flexstore.domain.user.administration.usecase.CreateUserRequest
import org.flexstore.domain.user.administration.usecase.UpdateUserRequest
import org.flexstore.infra.spring.adapter.mapping.toJsonUser
import org.flexstore.infra.spring.adapter.mapping.toJsonUsers
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonAccountStatus
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
class UserAdministrationController(
    private val userService: UserService,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created",
        content = [Content(schema = Schema(implementation = DefinedJsonUser::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@AuthenticationPrincipal creator: Jwt, @RequestBody draftJsonUser: DraftJsonUser): JsonUser {
        val actor = actorResolutionService.actorOf(UserId.of(creator.subject))
        val createUserRequest = CreateUserRequest(actor, draftJsonUser.toUser())
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
        val actor = actorResolutionService.actorOf(UserId.of(reader.subject))
        val users = userService.readAllUsers(actor, PageRequest.of(page, size))
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
        val actor = actorResolutionService.actorOf(UserId.of(reader.subject))
        return userService.readUser(actor, ValidUserId(id)).toJsonUser()
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
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        userService.updateUser(UpdateUserRequest(actor, editedUser.toUser(id)))
        return userService.readUser(actor, UserId.of(id)).toJsonUser()
    }

    @Operation(
        summary = "Change the status of an account: ACTIVE, SUSPENDED, LOCKED or DISABLED (administrator only)"
    )
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    fun changeAccountStatus(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody status: JsonAccountStatus
    ): JsonUser {
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        val accountStatusChange = AccountStatusChange(actor, UserId.of(id), status.toAccountStatus())
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
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        userService.deleteUser(actor, ValidUserId(id))
    }
}
