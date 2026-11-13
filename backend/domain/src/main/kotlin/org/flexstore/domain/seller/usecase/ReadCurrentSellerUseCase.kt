package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the current seller: the one the next invoices will be issued with
class ReadCurrentSellerUseCase(
    private val sellerRepository: SellerRepository
) : QueryUseCase<SellerReading, Seller> {

    override fun getPreConditions(): PreConditions<SellerReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<SellerReading, Seller> { sellerRepository.current() }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<SellerReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can read the seller written on the invoices.")
            throw SellerAdministrationForbiddenException(message)
        }
    }
}
