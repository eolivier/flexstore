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

// A postal address: its recipient (the company is optional) and where it is (the complement is optional)
data class JsonPostalAddress @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("companyName") val companyName: String? = null,
    @JsonProperty("street") val street: String,
    @JsonProperty("complement") val complement: String? = null,
    @JsonProperty("postalCode") val postalCode: String,
    @JsonProperty("city") val city: String,
    @JsonProperty("country") val country: String
) {
    companion object {
        fun of(address: PostalAddress): JsonPostalAddress {
            val recipient = address.recipient
            val location = address.location
            return JsonPostalAddress(
                firstName = recipient.fullName.firstName.value,
                lastName = recipient.fullName.lastName.value,
                companyName = recipient.companyName.value.ifEmpty { null },
                street = location.street.value,
                complement = location.complement?.value,
                postalCode = location.postalCode.value,
                city = location.city.value,
                country = location.country.value
            )
        }
    }

    fun toPostalAddress(): PostalAddress {
        val fullName = FullName(FirstName(firstName.trim()), LastName(lastName.trim()))
        val recipient = Recipient(fullName, CompanyName(companyName?.trim().orEmpty()))
        val location = Location(
            street = Street(street.trim()),
            complement = complement?.trim()?.ifEmpty { null }?.let { AddressComplement(it) },
            postalCode = PostalCode(postalCode.trim()),
            city = City(city.trim()),
            country = Country(country.trim())
        )
        return PostalAddress(recipient, location)
    }
}

// The token of the verification link
data class JsonEmailVerification @JsonCreator constructor(@JsonProperty("token") val token: String)
