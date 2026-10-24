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

// The postal addresses of a customer: the first one is given when they create their account
data class PostalAddresses(private val addresses: List<PostalAddress>) {

    constructor(vararg addresses: PostalAddress) : this(addresses.toList())

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<PostalAddress> = addresses.toList()

    companion object {
        // A user created by an administrator has no postal address
        fun none() = PostalAddresses(emptyList())
    }
}
