package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.entity.of
import org.flexstore.domain.service.UserService
import org.flexstore.domain.valueobject.Name
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserRestController(val userService: UserService) {

    @GetMapping
    fun getAllUsers(): List<JsonUser> = userService.readAllUsers().toJsonUsers()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@RequestBody jsonUser: JsonUser): User {
        return userService.createUser(jsonUser.toUser())
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): User {
        val userId = ValidUserId(id)
        return userService.readUser(userId)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateUser(@PathVariable id: String, @RequestBody jsonUser: JsonUser) {
        userService.updateUser(jsonUser.toUser(id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable id: String) {
        val userId = ValidUserId(id)
        userService.deleteUser(userId)
    }
}

private fun <E> List<E>.toJsonUsers() = this.map {
    when (it) {
        is User.DefinedUser -> JsonUser(it.id.value, it.name.value, it.email.value)
        is User.UndefinedUser -> JsonUser(it.id.value, "Undefined User", "${it.reason}")
        else -> throw IllegalArgumentException("Unknown user type: ${it!!::class.java}")
    }
}

data class JsonUser @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String
) {
    fun toUser() = User.of(UserId.of(id), Name(name), Email(email))
    fun toUser(userId: String) = User.of(UserId.of(userId), Name(name), Email(email))
}