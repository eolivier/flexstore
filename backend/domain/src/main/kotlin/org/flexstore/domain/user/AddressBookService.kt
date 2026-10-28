package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

// Domain service: a user sees, edits and removes their addresses, which are read back, all through the use cases
class AddressBookService(
    private val readAccountUseCase: ReadAccountUseCase,
    private val addressUseCases: AddressUseCases
) {
    fun getAddresses(userId: UserId): PostalAddresses = readAccountUseCase.unfold(userId).postalAddresses

    fun change(addressChange: AddressChange): PostalAddresses {
        addressUseCases.changePostalAddressUseCase.unfold(addressChange)
        return getAddresses(addressChange.userAddress.userId)
    }

    fun remove(userAddress: UserAddress): PostalAddresses {
        val addressesBefore = getAddresses(userAddress.userId)
        addressUseCases.removePostalAddressUseCase.unfold(AddressRemoval(userAddress, addressesBefore))
        return getAddresses(userAddress.userId)
    }
}

// The use cases of the addresses of a user
data class AddressUseCases(
    val changePostalAddressUseCase: ChangePostalAddressUseCase,
    val removePostalAddressUseCase: RemovePostalAddressUseCase
)
