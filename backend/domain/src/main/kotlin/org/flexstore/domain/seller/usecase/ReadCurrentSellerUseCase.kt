package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// An administrator reads the current seller: the one the next invoices will be issued with
class ReadCurrentSellerUseCase(
    private val sellerRepository: SellerRepository,
    private val userRepository: UserRepository
) : QueryUseCase<SellerReading, Seller> {

    override fun getPreConditions(): PreConditions<SellerReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<SellerReading, Seller> { sellerRepository.current() }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<SellerReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw SellerAdministrationForbiddenException(
                ErrorMessage("Only an administrator can read the seller written on the invoices.")
            )
        }
    }
}
