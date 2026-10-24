package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.user.model.Account

// The account of the signed-in user, with their contact details (never the password)
data class JsonAccount(
    @JsonProperty("id") val id: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("role") val role: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("phoneNumber") val phoneNumber: String?,
    @JsonProperty("postalAddresses") val postalAddresses: List<JsonPostalAddress>
) {
    constructor(account: Account) : this(
        account.id.value,
        account.fullName.firstName.value,
        account.fullName.lastName.value,
        account.email.value,
        account.role.name,
        account.status.name,
        account.phoneNumber?.value,
        account.postalAddresses.asList().map { JsonPostalAddress.of(it) }
    )
}
