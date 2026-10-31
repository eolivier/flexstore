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

// Who the seller is and where it is
data class SellerContact(val name: SellerName, val address: SellerAddress) {
    fun isComplete() = name.isGiven() && address.isGiven()

    fun isIncomplete() = !isComplete()
}

// How the seller is registered
data class SellerIdentifiers(val siret: Siret, val vatNumber: SellerVatNumber) {
    fun areValid() = siret.isValid() && vatNumber.isValid()

    fun areInvalid() = !areValid()
}

// What the administrators write about the seller: who it is, and how it is registered
data class SellerDetails(val contact: SellerContact, val identifiers: SellerIdentifiers)

// The shop that sells, as written on its invoices, in one of its versions
data class Seller(val id: SellerId, val details: SellerDetails)

data class InvalidSellerException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class SellerUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class SellerNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

private val SIRET = Regex("[0-9]{14}")
private val VAT_NUMBER = Regex("[A-Z]{2}[0-9A-Z]{2,13}")
