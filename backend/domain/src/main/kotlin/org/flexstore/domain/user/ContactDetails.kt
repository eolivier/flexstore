package org.flexstore.domain.user

data class PhoneNumber(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// A postal address: the complement (building, floor...) is optional
data class PostalAddress(
    val street: String,
    val complement: String?,
    val postalCode: String,
    val city: String,
    val country: String
) {
    fun isComplete() = listOf(street, postalCode, city, country).none { it.isBlank() }
}
