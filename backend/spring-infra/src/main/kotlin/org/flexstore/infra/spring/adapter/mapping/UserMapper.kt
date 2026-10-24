package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonUser

fun List<User>.toJsonUsers() = this.map { it.toJsonUser() }

fun User.toJsonUser(): JsonUser = when (this) {
    is User.DefinedUser -> DefinedJsonUser(this.id.value, this.name.value, this.email.value, role = this.role.name)
    is User.UndefinedUser -> DefinedJsonUser(this.id.value, "Undefined User", this.reason)
    is User.DraftUser -> DefinedJsonUser(this.id.value, this.name.value, this.email.value)
}