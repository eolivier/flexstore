package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.InvalidStatusChangeException
import org.ucop.domain.ErrorMessage

// The status an administrator gives to an account
data class JsonAccountStatus @JsonCreator constructor(@JsonProperty("status") val status: String) {
    fun toAccountStatus(): AccountStatus =
        AccountStatus.entries.find { it.name == status }
            ?: throw InvalidStatusChangeException(ErrorMessage("Unknown account status: $status"))
}
