package org.flexstore.domain.seller

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.Identity
import org.ucop.domain.NominalException

// One version of the seller: each edit gives a new one, so that an invoice keeps the seller of its issue
data class SellerId(val value: Identity) {
    companion object {
        fun nextId() = SellerId(Identity.nextId())
    }
}

data class SellerName(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// The address of the seller, on one line
data class SellerAddress(val value: String) {
    fun isGiven() = value.isNotBlank()
}

// The SIRET of the seller (the French identifier of its establishment): 14 digits, maybe grouped by spaces
data class Siret(val value: String) {
    fun isValid() = SIRET.matches(value.replace(" ", ""))
}

// The intra-community VAT number of the seller: its country (FR), then 2 to 13 digits or letters
data class SellerVatNumber(val value: String) {
    fun isValid() = VAT_NUMBER.matches(value.replace(" ", "").uppercase())
}

// The phone number of the seller: digits, maybe after a +, grouped by spaces, dots, dashes or parentheses
data class SellerPhone(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = PHONE.matches(value)
}

// The email the customers write to
data class SellerEmail(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = EMAIL.matches(value)
}

// The website of the shop, with or without its scheme (https://www.flexstore.fr, www.flexstore.fr)
data class SellerWebsite(val value: String) {
    fun isGiven() = value.isNotBlank()

    fun isValid() = WEBSITE.matches(value)
}

// How the customers reach the seller. The versions of the seller written before these were asked have none
data class SellerReach(val phone: SellerPhone, val email: SellerEmail, val website: SellerWebsite) {
    fun isValid() = phone.isValid() && email.isValid() && website.isValid()

    fun isInvalid() = !isValid()

    companion object {
        fun none() = SellerReach(SellerPhone(""), SellerEmail(""), SellerWebsite(""))
    }
}

// Who the seller is and where it is
data class SellerContact(val name: SellerName, val address: SellerAddress) {
    fun isComplete() = name.isGiven() && address.isGiven()

    fun isIncomplete() = !isComplete()
}

// How the seller is registered
data class SellerIdentifier(val siret: Siret, val vatNumber: SellerVatNumber) {
    fun isValid() = siret.isValid() && vatNumber.isValid()

    fun isInvalid() = !isValid()
}

// What the administrators write about the seller: who it is, how it is registered, and how to reach it
data class SellerDetail(val contact: SellerContact, val identifier: SellerIdentifier, val reach: SellerReach)

// The shop that sells, as written on its invoices, in one of its versions
data class Seller(val id: SellerId, val detail: SellerDetail)

data class InvalidSellerException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class SellerUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class SellerNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

private val SIRET = Regex("[0-9]{14}")
private val VAT_NUMBER = Regex("[A-Z]{2}[0-9A-Z]{2,13}")
private val PHONE = Regex("\\+?[0-9][0-9 .()-]{5,19}")
private val EMAIL = Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
private val WEBSITE = Regex("(https?://)?[^\\s/.]+(\\.[^\\s/.]+)+(/\\S*)?")
