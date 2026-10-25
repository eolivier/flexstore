package org.flexstore.domain.user.model

data class PhoneNumber(val value: String) {
    fun isGiven() = value.isNotBlank()
}

data class Street(val value: String)

// Building, floor, apartment...
data class AddressComplement(val value: String)

data class PostalCode(val value: String)

data class City(val value: String)

data class Country(val value: String)

// The company of a recipient, when the mail is sent to a company
data class CompanyName(val value: String) {
    fun isGiven() = value.isNotBlank()

    companion object {
        // A recipient who is a person, not a company
        fun none() = CompanyName("")
    }
}

// Who receives what is sent to an address: maybe someone else than the user (a gift, a company)
data class Recipient(val fullName: FullName, val companyName: CompanyName) {

    constructor(fullName: FullName) : this(fullName, CompanyName.none())

    fun isComplete() = fullName.isComplete()
}

// Where an address is: the complement is optional
data class Location(
    val street: Street,
    val complement: AddressComplement?,
    val postalCode: PostalCode,
    val city: City,
    val country: Country
) {
    fun isComplete() = listOf(street.value, postalCode.value, city.value, country.value).none { it.isBlank() }
}

// A postal address: who receives what is sent there, and where it is
data class PostalAddress(val recipient: Recipient, val location: Location) {
    fun isComplete() = recipient.isComplete() && location.isComplete()
}

// The postal addresses of a customer: the first one is given when they create their account
data class PostalAddresses(private val addresses: List<PostalAddress>) {

    constructor(vararg addresses: PostalAddress) : this(addresses.toList())

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<PostalAddress> = addresses.toList()

    // The given addresses are added at the end, unless they are already there
    fun including(vararg newAddresses: PostalAddress): PostalAddresses {
        val includedAddresses = newAddresses.fold(addresses) { kept, address -> if (address in kept) kept else kept + address }
        return PostalAddresses(includedAddresses)
    }

    companion object {
        // A user created by an administrator has no postal address
        fun none() = PostalAddresses(emptyList())
    }
}
