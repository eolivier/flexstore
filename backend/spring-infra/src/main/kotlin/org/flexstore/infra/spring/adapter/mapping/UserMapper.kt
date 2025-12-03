package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.entity.User
import org.flexstore.infra.spring.adapter.rest.json.JsonUser

fun <E> List<E>.toJsonUsers() = this.map {
    when (it) {
        is User.DefinedUser -> JsonUser(it.id.value, it.name.value, it.email.value)
        is User.UndefinedUser -> JsonUser(it.id.value, "Undefined User", "${it.reason}")
        else -> throw IllegalArgumentException("Unknown user type: ${it!!::class.java}")
    }
}

fun User.toJsonUser(): JsonUser = when (this) {
    is User.DefinedUser -> JsonUser(this.id.value, this.name.value, this.email.value)
    is User.UndefinedUser -> JsonUser(this.id.value, "Undefined User", this.reason)
}