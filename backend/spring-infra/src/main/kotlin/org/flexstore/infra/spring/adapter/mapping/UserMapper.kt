package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.user.*
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.JsonUser

fun List<User>.toJsonUsers() = this.map { it.toJsonUser() }

fun User.toJsonUser(): JsonUser = when (this) {
    is User.DefinedUser -> DefinedJsonUser(this.id.value, this.fullName.firstName.value, this.fullName.lastName.value, this.email.value, role = this.role.name, status = this.status.name)
    is User.UndefinedUser -> DefinedJsonUser(this.id.value, "Undefined", "User", this.reason)
    is User.DraftUser -> DefinedJsonUser(this.id.value, this.fullName.firstName.value, this.fullName.lastName.value, this.email.value)
}
fun Account.toJsonUser(): JsonUser =
    DefinedJsonUser(id.value, fullName.firstName.value, fullName.lastName.value, email.value, role = role.name, status = status.name)
