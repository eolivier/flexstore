package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// The SIREN of a company, its French identifier: 9 digits
data class Siren(val value: String) {

    fun isValid() = SIREN.matches(value)

    companion object {
        private val SIREN = Regex("[0-9]{9}")

        // As typed: the spaces are dropped (123 456 789)
        fun from(typedSiren: String) = Siren(typedSiren.replace(" ", ""))
    }
}

// The company a customer buys for: its name, its SIREN, and its intra-community VAT number when it gives one
// (a French one agrees with the SIREN)
data class CustomerCompany(val company: Company, val siren: Siren) {

    val name get() = company.name

    val vatNumber get() = company.vatNumber

    fun isValid() = company.isComplete() && siren.isValid() && vatNumberIsValid()

    private fun vatNumberIsValid() =
        vatNumber.isNotGiven() || (vatNumber.isWellFormed() && vatNumber.agreesWith(siren))
}

// Who a customer buys for: themselves, or a company
sealed interface CustomerType {

    fun isValid(): Boolean

    fun isInvalid() = !isValid()

    // The recipient of the address given at the creation of the account: the customer, or their company for
    // their attention
    fun recipientFor(fullName: FullName): Recipient

    data object Individual : CustomerType {
        override fun isValid() = true

        override fun recipientFor(fullName: FullName) = PrivateIndividual(fullName)
    }

    data class Business(val company: CustomerCompany) : CustomerType {
        override fun isValid() = company.isValid()

        override fun recipientFor(fullName: FullName) = LegalEntity(company.company, fullName)
    }
}

data class InvalidCompanyException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
