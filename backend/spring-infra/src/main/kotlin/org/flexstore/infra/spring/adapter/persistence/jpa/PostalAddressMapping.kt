package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.AddressEntity
import org.flexstore.domain.user.model.*

private const val PRIVATE_INDIVIDUAL = "PRIVATE_INDIVIDUAL"
private const val LEGAL_ENTITY = "LEGAL_ENTITY"

// The postal address stored in a row of the address table (an address of users, maybe used by orders)
internal fun AddressEntity.toDomain(): PostalAddress {
    val addressComplement = complement?.let { AddressComplement(it) }
    val streetAddress = StreetAddress(Street(street), addressComplement)
    val town = Town(PostalCode(postalCode), City(city))
    val location = Location(streetAddress, Locality(town, Country(country)))
    return PostalAddress(recipient(), location)
}

private fun AddressEntity.recipient(): Recipient {
    val person = FullName(FirstName(firstName.orEmpty()), LastName(lastName.orEmpty()))
    val company = Company(CompanyName(companyName.orEmpty()), VatNumber(vatNumber.orEmpty()))
    return when (recipientType) {
        LEGAL_ENTITY -> LegalEntity(company, person)
        else -> PrivateIndividual(person)
    }
}

// A new row of the address table: the company columns stay empty for a person, the names maybe for a company
internal fun PostalAddress.toEntity(id: String): AddressEntity {
    val legalEntity = recipient as? LegalEntity
    val person = legalEntity?.contact ?: (recipient as PrivateIndividual).fullName
    return AddressEntity(
        id,
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
