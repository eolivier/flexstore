package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// A user removes one of their addresses. The orders keep the address they were delivered and billed to
class RemovePostalAddressUseCase(private val userRepository: UserRepository) : UseCase<AddressRemoval> {

    override fun getPreConditions(): PreConditions<AddressRemoval> =
        PreConditions(
            userAddressExistsCondition(userRepository) { it.userAddress },
            addressIsTheOneShownCondition()
        )

    override fun getNominalScenario(): NominalScenario<AddressRemoval> {
        val removeStep = Step<AddressRemoval> { addressRemoval ->
            val userAddress = addressRemoval.userAddress
            val definedUser = userRepository.findById(userAddress.userId) as User.DefinedUser
            val remainingAddresses = definedUser.postalAddresses.withoutAt(userAddress.position)
            userRepository.save(definedUser.withPostalAddresses(remainingAddresses))
        }
        return NominalScenario(removeStep)
    }

    override fun getPostConditions(): PostConditions<AddressRemoval> =
        PostConditions(addressIsRemovedCondition())

    // The addresses did not change since they were read (in another tab, for example)
    private fun addressIsTheOneShownCondition() = PreCondition<AddressRemoval> { addressRemoval ->
        val userAddress = addressRemoval.userAddress
        val definedUser = userRepository.findById(userAddress.userId) as User.DefinedUser
        if (definedUser.postalAddresses != addressRemoval.addressesBefore) {
            throw AddressNotFoundException(ErrorMessage("The addresses of ${userAddress.userId.value} changed."))
        }
    }

    private fun addressIsRemovedCondition() = PostCondition<AddressRemoval> { addressRemoval ->
        val userAddress = addressRemoval.userAddress
        val definedUser = userRepository.findById(userAddress.userId) as User.DefinedUser
        if (definedUser.postalAddresses != addressRemoval.addressesAfter()) {
            throw AddressChangeFailed(ErrorMessage("The address ${userAddress.position.value} was not removed."))
        }
    }
}
