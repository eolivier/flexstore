package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.user.model.*

// The postal address stored in a row of the address table (an address of a user, maybe used by their orders)
internal fun AddressEntity.toDomain() = PostalAddress(
    Street(street), complement?.let { AddressComplement(it) }, PostalCode(postalCode), City(city), Country(country)
)
