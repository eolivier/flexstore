package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class JsonChangePasswordRequest @JsonCreator constructor(
    @JsonProperty("currentPassword") val currentPassword: String,
    @JsonProperty("newPassword") val newPassword: String
) {
    fun toChangePasswordRequest(userId: String) =
        ChangePasswordRequest(UserId.of(userId), PlainPassword(currentPassword), PlainPassword(newPassword))
}
