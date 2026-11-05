package org.flexstore.domain.user.address.usecase

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
        PreConditions(userExistsCondition(), newAddressIsCompleteCondition())

    override fun getNominalScenario(): NominalScenario<AddressAddition> {
        val addStep = Step<AddressAddition> { addressAddition ->
            val definedUser = userRepository.findById(addressAddition.userId) as User.DefinedUser
            userRepository.save(definedUser.withPostalAddressesIncluding(addressAddition.newAddress))
        }
        return NominalScenario(addStep)
    }

    override fun getPostConditions(): PostConditions<AddressAddition> = PostConditions(addressIsAddedCondition())

    private fun userExistsCondition() = PreCondition<AddressAddition> { addressAddition ->
        if (userRepository.notExists(addressAddition.userId)) {
            throw UserNotFoundException(ErrorMessage("User ${addressAddition.userId.value} not found."))
        }
    }

    private fun newAddressIsCompleteCondition() = PreCondition<AddressAddition> { addressAddition ->
        if (addressAddition.newAddress.isComplete().not()) {
            val message = "An address needs a recipient, a street, a city and a country."
            throw IncompleteAddressException(ErrorMessage(message))
        }
    }

    private fun addressIsAddedCondition() = PostCondition<AddressAddition> { addressAddition ->
        val definedUser = userRepository.findById(addressAddition.userId) as User.DefinedUser
        if (definedUser.postalAddresses.includes(addressAddition.newAddress).not()) {
            val message = "The address was not added for ${addressAddition.userId.value}."
            throw AddressChangeFailed(ErrorMessage(message))
        }
    }
}
