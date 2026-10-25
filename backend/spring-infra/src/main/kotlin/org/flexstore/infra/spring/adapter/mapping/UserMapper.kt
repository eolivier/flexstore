package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonUser

fun List<User>.toJsonUsers() = this.map { it.toJsonUser() }

fun User.toJsonUser(): JsonUser = when (this) {
    is User.DefinedUser -> account().toJsonUser()
    is User.UndefinedUser -> DefinedJsonUser(this.id.value, "Undefined", "User", this.reason)
    is User.DraftUser -> {
        val (firstName, lastName) = fullName
        DefinedJsonUser(id.value, firstName.value, lastName.value, email.value)
    }
}
fun Account.toJsonUser(): JsonUser {
    val (firstName, lastName) = fullName
    val jsonUser = DefinedJsonUser(id.value, firstName.value, lastName.value, email.value)
    return jsonUser.copy(role = role.name, status = status.name)
}
