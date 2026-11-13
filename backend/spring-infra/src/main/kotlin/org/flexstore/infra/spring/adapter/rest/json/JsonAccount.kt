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
    @JsonProperty("postalAddresses") val postalAddresses: List<JsonPostalAddress>,
    // The company of a customer who buys for it, none for the others
    @JsonProperty("company") val company: JsonCustomerCompany?,
    // Restricted to an ADMIN account in this iteration
    @JsonProperty("twoFactorEnabled") val twoFactorEnabled: Boolean
) {
    constructor(account: Account) : this(
        account.id.value,
        account.fullName.firstName.value,
        account.fullName.lastName.value,
        account.email.value,
        account.role.name,
        account.status.name,
        if (account.phoneNumber.isGiven()) account.phoneNumber.value else null,
        account.postalAddresses.asList().map { JsonPostalAddress.of(it) },
        JsonCustomerCompany.of(account.customerType),
        account.twoFactorAuthentication.isEnabled()
    )
}
