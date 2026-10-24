package org.flexstore.domain.user

data class PhoneNumber(val value: String) {
    fun isGiven() = value.isNotBlank()
}

data class Street(val value: String)

// Building, floor, apartment...
data class AddressComplement(val value: String)

data class PostalCode(val value: String)

data class City(val value: String)

data class Country(val value: String)

// A postal address: the complement is optional
data class PostalAddress(
    val street: Street,
    val complement: AddressComplement?,
    val postalCode: PostalCode,
    val city: City,
    val country: Country
) {
    fun isComplete() = listOf(street.value, postalCode.value, city.value, country.value).none { it.isBlank() }
}
