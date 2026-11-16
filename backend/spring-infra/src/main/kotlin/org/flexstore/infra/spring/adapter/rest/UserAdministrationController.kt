package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.sharedkernel.PageNumber
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.PageSize
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserSearchText
import org.flexstore.domain.user.model.UsersSearch
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

@RestController
@RequestMapping("/api/users")
class UserAdministrationController(
    private val userService: UserService,
    private val actorResolutionService: ActorResolutionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@AuthenticationPrincipal creator: Jwt, @RequestBody draftJsonUser: DraftJsonUser): JsonUser {
        val actor = actorResolutionService.actorOf(creator)
        val createUserRequest = CreateUserRequest(actor, draftJsonUser.toUser())
        return userService.createUser(createUserRequest).toJsonUser()
    }

    @GetMapping
    fun getAllUsers(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "${PageNumber.FIRST}") page: Int,
        @RequestParam(defaultValue = "${PageSize.DEFAULT}") size: Int,
        @RequestParam(defaultValue = "") search: String
    ): ResponseEntity<List<JsonUser>> {
        val actor = actorResolutionService.actorOf(reader)
        val usersSearch = UsersSearch(UserSearchText(search), PageRequest.of(page, size))
        val users = userService.readAllUsers(actor, usersSearch)
        return pagedBody(users.content.asList().toJsonUsers(), users.total)
    }

    @GetMapping("/{id}")
    fun getUser(@AuthenticationPrincipal reader: Jwt, @PathVariable id: String): JsonUser {
        val actor = actorResolutionService.actorOf(reader)
        return userService.readUser(actor, ValidUserId(id)).toJsonUser()
    }

    @PutMapping("/{id}")
    fun updateUser(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody editedUser: EditedJsonUser
    ): JsonUser {
        val actor = actorResolutionService.actorOf(editor)
        userService.updateUser(UpdateUserRequest(actor, editedUser.toUser(id)))
        return userService.readUser(actor, UserId.of(id)).toJsonUser()
    }

    @PutMapping("/{id}/status")
    fun changeAccountStatus(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody status: JsonAccountStatus
    ): JsonUser {
        val actor = actorResolutionService.actorOf(editor)
        val accountStatusChange = AccountStatusChange(actor, UserId.of(id), status.toAccountStatus())
        return userService.changeAccountStatus(accountStatusChange).toJsonUser()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@AuthenticationPrincipal editor: Jwt, @PathVariable id: String) {
        val actor = actorResolutionService.actorOf(editor)
        userService.deleteUser(actor, ValidUserId(id))
    }
}
