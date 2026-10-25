package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.user.model.*

// The postal address stored in a row of the address table (an address of users, maybe used by orders)
internal fun AddressEntity.toDomain(): PostalAddress {
    val fullName = FullName(FirstName(firstName), LastName(lastName))
    val recipient = Recipient(fullName, CompanyName(companyName.orEmpty()))
    val complement = complement?.let { AddressComplement(it) }
    val location = Location(Street(street), complement, PostalCode(postalCode), City(city), Country(country))
    return PostalAddress(recipient, location)
}

// A new row of the address table
internal fun PostalAddress.toEntity(id: String): AddressEntity {
    val fullName = recipient.fullName
    return AddressEntity(
        id = id,
        firstName = fullName.firstName.value,
        lastName = fullName.lastName.value,
        companyName = recipient.companyName.value.ifEmpty { null },
        street = location.street.value,
        complement = location.complement?.value,
        postalCode = location.postalCode.value,
        city = location.city.value,
        country = location.country.value
    )
}
