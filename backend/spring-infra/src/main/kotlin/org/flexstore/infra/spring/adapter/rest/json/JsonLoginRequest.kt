package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.PlainPassword
import org.flexstore.domain.usecase.user.LoginRequest

data class JsonLoginRequest @JsonCreator constructor(
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String
) {
    fun toLoginRequest() = LoginRequest(Email(email), PlainPassword(password))
}
