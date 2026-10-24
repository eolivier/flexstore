package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class JsonLoginRequest @JsonCreator constructor(
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String
) {
    fun toLoginRequest() = LoginRequest(Email(email), PlainPassword(password))
}
