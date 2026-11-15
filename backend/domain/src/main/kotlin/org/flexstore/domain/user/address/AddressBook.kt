package org.flexstore.domain.user.address

import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor

// The place of an address among the addresses of a user (0 for the first one)
data class AddressPosition(val value: Int)

// An address of a customer, by its place among their addresses
data class UserAddress(val customer: Actor, val position: AddressPosition)

// A customer's addresses are read
data class PostalAddressesReading(val customer: Actor)

// A customer adds a new address to their address book
data class AddressAddition(val customer: Actor, val newAddress: PostalAddress)

// A user gives a new version of one of their addresses
data class AddressChange(val userAddress: UserAddress, val newAddress: PostalAddress)

// A user removes one of their addresses, from their addresses as they were before
data class AddressRemoval(val userAddress: UserAddress, val addressesBefore: PostalAddresses) {
    fun addressesAfter() = addressesBefore.withoutAt(userAddress.position)
}

data class AddressNotFoundException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class IncompleteAddressException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class AddressChangeFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
