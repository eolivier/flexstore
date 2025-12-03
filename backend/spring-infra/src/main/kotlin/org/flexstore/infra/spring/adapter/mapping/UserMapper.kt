package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.entity.User
import org.flexstore.infra.spring.adapter.rest.json.JsonUser

fun List<User>.toJsonUsers() = this.map { it.toJsonUser() }

fun User.toJsonUser(): JsonUser = when (this) {
    is User.DefinedUser -> JsonUser(this.id.value, this.name.value, this.email.value)
    is User.UndefinedUser -> JsonUser(this.id.value, "Undefined User", this.reason)
}