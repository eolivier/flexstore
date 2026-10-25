package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.user.model.*

// The account a customer creates for themselves
data class JsonAccountRegistration @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("phoneNumber") val phoneNumber: String,
    @JsonProperty("postalAddress") val postalAddress: JsonPostalAddress
) {
    fun toRegistration(userId: UserId) = CustomerRegistration(
        userId = userId,
        fullName = FullName(FirstName(firstName.trim()), LastName(lastName.trim())),
        email = Email(email.trim()),
        password = PlainPassword(password),
        phoneNumber = PhoneNumber(phoneNumber.trim()),
        postalAddress = postalAddress.toPostalAddress()
    )
}

data class JsonPostalAddress @JsonCreator constructor(
    @JsonProperty("street") val street: String,
    @JsonProperty("complement") val complement: String? = null,
    @JsonProperty("postalCode") val postalCode: String,
    @JsonProperty("city") val city: String,
    @JsonProperty("country") val country: String
) {
    companion object {
        fun of(address: PostalAddress) = JsonPostalAddress(
            address.street.value, address.complement?.value, address.postalCode.value, address.city.value, address.country.value
        )
    }

    fun toPostalAddress() = PostalAddress(
        street = Street(street.trim()),
        complement = complement?.trim()?.ifEmpty { null }?.let { AddressComplement(it) },
        postalCode = PostalCode(postalCode.trim()),
        city = City(city.trim()),
        country = Country(country.trim())
    )
}

// The token of the verification link
data class JsonEmailVerification @JsonCreator constructor(@JsonProperty("token") val token: String)
