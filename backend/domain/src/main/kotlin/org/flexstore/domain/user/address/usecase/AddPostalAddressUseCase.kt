package org.flexstore.domain.user.address.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*
import org.flexstore.domain.user.address.AddressAddition
import org.flexstore.domain.user.address.AddressChangeFailed
import org.flexstore.domain.user.address.IncompleteAddressException

// A user adds a new address to their address book
class AddPostalAddressUseCase(private val userRepository: UserRepository) : UseCase<AddressAddition> {

    override fun getPreConditions(): PreConditions<AddressAddition> =
        PreConditions(customerCanActCondition(), newAddressIsCompleteCondition())

    override fun getNominalScenario(): NominalScenario<AddressAddition> {
        val addStep = Step<AddressAddition> { addressAddition ->
            val definedUser = userRepository.findById(addressAddition.customer.customerId()) as User.DefinedUser
            userRepository.save(definedUser.withPostalAddressesIncluding(addressAddition.newAddress))
        }
        return NominalScenario(addStep)
    }

    override fun getPostConditions(): PostConditions<AddressAddition> = PostConditions(addressIsAddedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<AddressAddition> { addressAddition ->
        addressAddition.customer.verifyCanAct()
    }

    private fun newAddressIsCompleteCondition() = PreCondition<AddressAddition> { addressAddition ->
        if (addressAddition.newAddress.isComplete().not()) {
            val message = "An address needs a recipient, a street, a city and a country."
            throw IncompleteAddressException(ErrorMessage(message))
        }
    }

    private fun addressIsAddedCondition() = PostCondition<AddressAddition> { addressAddition ->
        val definedUser = userRepository.findById(addressAddition.customer.customerId()) as User.DefinedUser
        if (definedUser.postalAddresses.includes(addressAddition.newAddress).not()) {
            val message = "The address was not added for ${addressAddition.customer.customerId().value}."
            throw AddressChangeFailed(ErrorMessage(message))
        }
    }
}
