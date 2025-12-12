package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.*
import org.flexstore.domain.valueobject.Name

data class JsonUser @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String? = null
) {
    fun toUser() = User.of(UserId.of(id), Name(name), Email(email), PlainPassword(password ?: ""))
    fun toUser(userId: String) = User.of(UserId.of(userId), Name(name), Email(email), PlainPassword(password ?: ""))
}