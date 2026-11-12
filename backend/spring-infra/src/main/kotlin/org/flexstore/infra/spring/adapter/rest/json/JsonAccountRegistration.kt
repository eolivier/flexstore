package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.email.model.VerificationToken
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.ChosenPassword

// The account a customer creates for themselves. The express registration only gives the names, the email and
// the password: the phone number and the postal address are optional
data class JsonAccountRegistration @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("password") val password: String,
    @JsonProperty("phoneNumber") val phoneNumber: String? = null,
    @JsonProperty("postalAddress") val postalAddress: JsonPostalAddress? = null,
    // Given by a customer who buys for their company; none for a customer who buys for themselves
    @JsonProperty("company") val company: JsonCustomerCompany? = null
) {
    fun toRegistration(userId: UserId): CustomerRegistration {
        val fullName = FullName(FirstName(firstName.trim()), LastName(lastName.trim()))
        val phone = PhoneNumber(phoneNumber.orEmpty().trim())
        val addresses = postalAddress?.let { PostalAddresses(it.toPostalAddress()) } ?: PostalAddresses.none()
        val customerType = company?.toCustomerType() ?: CustomerType.Individual
        return CustomerRegistration(
            userId, fullName, Email(email.trim()), PlainPassword(password), phone, addresses, customerType
        )
    }
}

// The company a customer buys for: its name, its SIREN (9 digits) and its intra-community VAT number (optional)
data class JsonCustomerCompany @JsonCreator constructor(
    @JsonProperty("companyName") val companyName: String,
    @JsonProperty("siren") val siren: String,
    @JsonProperty("vatNumber") val vatNumber: String? = null
) {
    fun toCustomerType(): CustomerType {
        val company = Company(CompanyName(companyName.trim()), VatNumber.from(vatNumber.orEmpty()))
        return CustomerType.Business(CustomerCompany(company, Siren.from(siren)))
    }

    companion object {
        // The company of a customer who buys for it, none for the others
        fun of(customerType: CustomerType): JsonCustomerCompany? = when (customerType) {
            is CustomerType.Business -> JsonCustomerCompany(
                customerType.company.name.value,
                customerType.company.siren.value,
                customerType.company.vatNumber.value.ifEmpty { null }
            )
            CustomerType.Individual -> null
        }
    }
}

// A postal address: its recipient and where it is (the complement is optional).
// The recipient is a person (PRIVATE_INDIVIDUAL: first name and last name)
// or a company (LEGAL_ENTITY: company name, VAT number and names of a contact when given)
data class JsonPostalAddress @JsonCreator constructor(
    @JsonProperty("recipientType") val recipientType: String,
    @JsonProperty("firstName") val firstName: String? = null,
    @JsonProperty("lastName") val lastName: String? = null,
    @JsonProperty("companyName") val companyName: String? = null,
    @JsonProperty("vatNumber") val vatNumber: String? = null,
    @JsonProperty("street") val street: String,
    @JsonProperty("complement") val complement: String? = null,
    @JsonProperty("postalCode") val postalCode: String,
    @JsonProperty("city") val city: String,
    @JsonProperty("country") val country: String
) {
    companion object {
        const val PRIVATE_INDIVIDUAL = "PRIVATE_INDIVIDUAL"
        const val LEGAL_ENTITY = "LEGAL_ENTITY"

        fun of(address: PostalAddress): JsonPostalAddress {
            val legalEntity = address.recipient as? LegalEntity
            val person = legalEntity?.contact ?: (address.recipient as PrivateIndividual).fullName
            val location = address.location
            return JsonPostalAddress(
                recipientType = if (legalEntity != null) LEGAL_ENTITY else PRIVATE_INDIVIDUAL,
                firstName = person.firstName.value.ifEmpty { null },
                lastName = person.lastName.value.ifEmpty { null },
                companyName = legalEntity?.company?.name?.value,
                vatNumber = legalEntity?.company?.vatNumber?.value?.ifEmpty { null },
                street = location.street.value,
                complement = location.complement?.value,
                postalCode = location.postalCode.value,
                city = location.city.value,
                country = location.country.value
            )
        }
    }

    fun toPostalAddress(): PostalAddress {
        val addressComplement = complement?.trim()?.ifEmpty { null }?.let { AddressComplement(it) }
        val streetAddress = StreetAddress(Street(street.trim()), addressComplement)
        val town = Town(PostalCode(postalCode.trim()), City(city.trim()))
        val location = Location(streetAddress, Locality(town, Country(country.trim())))
        return PostalAddress(recipient(), location)
    }

    private fun recipient(): Recipient = when (recipientType) {
        LEGAL_ENTITY -> legalEntity()
        else -> privateIndividual()
    }

    private fun legalEntity(): LegalEntity {
        val company = Company(CompanyName(companyName?.trim().orEmpty()), VatNumber(vatNumber?.trim().orEmpty()))
        return LegalEntity(company, person())
    }

    private fun privateIndividual() = PrivateIndividual(person())

    private fun person() = FullName(FirstName(firstName?.trim().orEmpty()), LastName(lastName?.trim().orEmpty()))
}

// The token of the verification link
data class JsonEmailVerification @JsonCreator constructor(@JsonProperty("token") val token: String)

// The email of the account whose password is forgotten
data class JsonPasswordResetRequest @JsonCreator constructor(@JsonProperty("email") val email: String)

// The token of the link to choose a new password, and the new password
data class JsonChosenPassword @JsonCreator constructor(
    @JsonProperty("token") val token: String,
    @JsonProperty("password") val password: String
) {
    fun toChosenPassword() = ChosenPassword(VerificationToken(token), PlainPassword(password))
}
