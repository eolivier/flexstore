package org.flexstore.domain.user.address.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import org.flexstore.domain.user.address.AddressChange
import org.flexstore.domain.user.address.AddressChangeFailed
import org.flexstore.domain.user.address.AddressNotFoundException
import org.flexstore.domain.user.address.IncompleteAddressException
import org.flexstore.domain.user.address.UserAddress

// A user edits one of their addresses. The orders keep the version they were delivered and billed to
class ChangePostalAddressUseCase(private val userRepository: UserRepository) : UseCase<AddressChange> {

    override fun getPreConditions(): PreConditions<AddressChange> =
        PreConditions(
            userAddressExistsCondition(userRepository) { it.userAddress },
            newAddressIsCompleteCondition()
        )

    override fun getNominalScenario(): NominalScenario<AddressChange> {
        val changeStep = Step<AddressChange> { addressChange ->
            val customerId = addressChange.userAddress.customer.customerId()
            val definedUser = userRepository.findById(customerId) as User.DefinedUser
            val position = addressChange.userAddress.position
            val changedAddresses = definedUser.postalAddresses.replacedAt(position, addressChange.newAddress)
            userRepository.save(definedUser.withPostalAddresses(changedAddresses))
        }
        return NominalScenario(changeStep)
    }

    override fun getPostConditions(): PostConditions<AddressChange> = PostConditions(addressIsChangedCondition())

    private fun newAddressIsCompleteCondition() = PreCondition<AddressChange> { addressChange ->
        if (addressChange.newAddress.isComplete().not()) {
            val message = "An address needs a recipient, a street, a city and a country."
            throw IncompleteAddressException(ErrorMessage(message))
        }
    }

    private fun addressIsChangedCondition() = PostCondition<AddressChange> { addressChange ->
        val userAddress = addressChange.userAddress
        val definedUser = userRepository.findById(userAddress.customer.customerId()) as User.DefinedUser
        if (definedUser.postalAddresses.at(userAddress.position) != addressChange.newAddress) {
            val message = "The address ${userAddress.position.value} was not changed."
            throw AddressChangeFailed(ErrorMessage(message))
        }
    }
}

// The customer can act and has an address at the given position
internal fun <T> userAddressExistsCondition(userRepository: UserRepository, userAddressOf: (T) -> UserAddress) =
    PreCondition<T> { request ->
        val userAddress = userAddressOf(request)
        userAddress.customer.verifyCanAct()
        val definedUser = userRepository.findById(userAddress.customer.customerId()) as User.DefinedUser
        if (definedUser.postalAddresses.hasNo(userAddress.position)) {
            val message = "No address ${userAddress.position.value} for this user."
            throw AddressNotFoundException(ErrorMessage(message))
        }
    }
