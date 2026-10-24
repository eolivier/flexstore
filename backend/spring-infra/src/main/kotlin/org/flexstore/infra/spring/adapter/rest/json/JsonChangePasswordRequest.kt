package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.PlainPassword
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.of
import org.flexstore.domain.usecase.user.ChangePasswordRequest

data class JsonChangePasswordRequest @JsonCreator constructor(
    @JsonProperty("currentPassword") val currentPassword: String,
    @JsonProperty("newPassword") val newPassword: String
) {
    fun toChangePasswordRequest(userId: String) =
        ChangePasswordRequest(UserId.of(userId), PlainPassword(currentPassword), PlainPassword(newPassword))
}
