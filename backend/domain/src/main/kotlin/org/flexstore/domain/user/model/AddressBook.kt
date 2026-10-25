package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// The place of an address among the addresses of a user (0 for the first one)
data class AddressPosition(val value: Int)

// An address of a user, by its place among their addresses
data class UserAddress(val userId: UserId, val position: AddressPosition)

// A user gives a new version of one of their addresses
data class AddressChange(val userAddress: UserAddress, val newAddress: PostalAddress)

// A user removes one of their addresses, from their addresses as they were before
data class AddressRemoval(val userAddress: UserAddress, val addressesBefore: PostalAddresses) {
    fun addressesAfter() = addressesBefore.withoutAt(userAddress.position)
}

data class AddressNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class IncompleteAddressException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class AddressChangeFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
