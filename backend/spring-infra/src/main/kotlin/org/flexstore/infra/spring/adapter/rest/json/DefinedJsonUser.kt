package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.user.model.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

interface JsonUser

data class DefinedJsonUser @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String? = null,
    // Only sent in responses: the role cannot be changed through the API
    @JsonProperty("role") val role: String? = null,
    // Only sent in responses: the status of the account
    @JsonProperty("status") val status: String? = null
): JsonUser {
    fun toUser() = User.of(
        UserId.of(id),
        FullName(FirstName(firstName), LastName(lastName)),
        Email(email),
        PlainPassword(password ?: "")
    )
    fun toUser(userId: String) = User.of(
        UserId.of(userId),
        FullName(FirstName(firstName), LastName(lastName)),
        Email(email),
        PlainPassword(password ?: "")
    )
}

// role: USER (default) or ADMIN
data class DraftJsonUser @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("role") val role: String? = null
): JsonUser {
    fun toUser() = User.DraftUser(
        fullName = FullName(FirstName(firstName), LastName(lastName)),
        email = Email(email),
        password = PlainPassword(password),
        role = Role.fromName(role)
    )
}

// The fields an administrator can edit: the password is changed by a separate operation
data class EditedJsonUser @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("role") val role: String? = null
) {
    // The password is not edited: the stored one is kept by the use case
    fun toUser(userId: String) = User.DefinedUser(
        id = UserId.of(userId),
        fullName = FullName(FirstName(firstName), LastName(lastName)),
        email = Email(email),
        password = PlainPassword(""),
        role = Role.fromName(role)
    )
}
