package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.of
import org.flexstore.domain.valueobject.Name

data class JsonUser @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String
) {
    fun toUser() = User.of(UserId.of(id), Name(name), Email(email))
    fun toUser(userId: String) = User.of(UserId.of(userId), Name(name), Email(email))
}