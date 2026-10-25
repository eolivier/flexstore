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

// The name of a company that receives what is sent to an address
data class CompanyName(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// The VAT identification number of a company: optional
data class VatNumber(val value: String) {
    fun isGiven() = value.isNotBlank()

    companion object {
        // A company that does not give its VAT number
        fun none() = VatNumber("")
    }
}

// Who receives what is sent to an address, maybe someone else than the user: a person or a company
sealed interface Recipient {
    fun isComplete(): Boolean
}

// A person, by their first name and last name
data class PrivateIndividual(val fullName: FullName) : Recipient {
    override fun isComplete() = fullName.isComplete()
}

// A company, by its name, with its VAT number when it gives it
data class Company(val name: CompanyName, val vatNumber: VatNumber) {

    constructor(name: CompanyName) : this(name, VatNumber.none())

    fun isComplete() = name.isGiven()
}

// A company, maybe for the attention of a person (a delivery): the names of the contact are optional
data class LegalEntity(val company: Company, val contact: FullName) : Recipient {

    constructor(company: Company) : this(company, FullName.none())

    override fun isComplete() = company.isComplete()
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
