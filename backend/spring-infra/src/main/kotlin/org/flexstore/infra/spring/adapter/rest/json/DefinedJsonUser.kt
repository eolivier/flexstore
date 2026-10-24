package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

interface JsonUser

data class DefinedJsonUser @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String? = null,
    // Only sent in responses: the role cannot be changed through the API
    @JsonProperty("role") val role: String? = null
): JsonUser {
    fun toUser() = User.of(
        UserId.of(id),
        Name(name),
        Email(email),
        PlainPassword(password ?: "")
    )
    fun toUser(userId: String) = User.of(
        UserId.of(userId),
        Name(name),
        Email(email),
        PlainPassword(password ?: "")
    )
}

// role: USER (default) or ADMIN
data class DraftJsonUser @JsonCreator constructor(
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("role") val role: String? = null
): JsonUser {
    fun toUser() = User.DraftUser(
        name = Name(name),
        email = Email(email),
        password = PlainPassword(password),
        role = Role.fromName(role)
    )
}