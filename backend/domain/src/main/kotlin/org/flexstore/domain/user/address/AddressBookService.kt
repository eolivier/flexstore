package org.flexstore.domain.user.address

import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor
import org.flexstore.domain.user.address.usecase.AddPostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ChangePostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ListPostalAddressesUseCase
import org.flexstore.domain.user.address.usecase.RemovePostalAddressUseCase

// Domain service: a user sees, edits and removes their addresses, which are read back, all through the use cases
class AddressBookService(private val addressUseCases: AddressUseCases) {

    fun getAddresses(customer: Actor): PostalAddresses =
        addressUseCases.listPostalAddressesUseCase.unfold(PostalAddressesReading(customer))

    fun add(addressAddition: AddressAddition): PostalAddresses {
        addressUseCases.addPostalAddressUseCase.unfold(addressAddition)
        return getAddresses(addressAddition.customer)
    }

    fun change(addressChange: AddressChange): PostalAddresses {
        addressUseCases.changePostalAddressUseCase.unfold(addressChange)
        return getAddresses(addressChange.userAddress.customer)
    }

    fun remove(userAddress: UserAddress): PostalAddresses {
        val addressesBefore = getAddresses(userAddress.customer)
        addressUseCases.removePostalAddressUseCase.unfold(AddressRemoval(userAddress, addressesBefore))
        return getAddresses(userAddress.customer)
    }
}

// The use cases of the addresses of a user
data class AddressUseCases(
    val listPostalAddressesUseCase: ListPostalAddressesUseCase,
    val addPostalAddressUseCase: AddPostalAddressUseCase,
    val changePostalAddressUseCase: ChangePostalAddressUseCase,
    val removePostalAddressUseCase: RemovePostalAddressUseCase
)
