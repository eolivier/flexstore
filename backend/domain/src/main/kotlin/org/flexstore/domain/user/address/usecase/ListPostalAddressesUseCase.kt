package org.flexstore.domain.user.address.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.user.address.PostalAddressesReading
import org.flexstore.domain.user.model.PostalAddresses
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.*

// A customer reads their addresses, in their order
class ListPostalAddressesUseCase(private val userRepository: UserRepository) :
    QueryUseCase<PostalAddressesReading, PostalAddresses> {

    override fun getPreConditions(): PreConditions<PostalAddressesReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<PostalAddressesReading, PostalAddresses> { reading ->
        (userRepository.findById(reading.customer.customerId()) as User.DefinedUser).postalAddresses
    }

    override fun getPostConditions(): PostConditions<PostalAddresses> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<PostalAddressesReading> { reading ->
        reading.customer.verifyCanAct()
    }
}
