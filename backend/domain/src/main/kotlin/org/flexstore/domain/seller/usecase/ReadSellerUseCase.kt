package org.flexstore.domain.seller.usecase

import org.flexstore.domain.actor.cannotReadPublicContent
import org.flexstore.domain.seller.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A version of the seller, as written on the invoices issued with it
// A reader asks for a version of the seller
data class SellerVersionReading(val reader: Actor, val sellerId: SellerId)

class ReadSellerUseCase(
    private val sellerRepository: SellerRepository
) : QueryUseCase<SellerVersionReading, Seller> {

    override fun getPreConditions(): PreConditions<SellerVersionReading> =
        PreConditions(readerCanReadCondition(), sellerExistsCondition())

    override fun getNominalQuery() =
        NominalQuery<SellerVersionReading, Seller> { reading ->
            sellerRepository.findBy(reading.sellerId)!!
        }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()

    private fun sellerExistsCondition() = PreCondition<SellerVersionReading> { reading ->
        if (sellerRepository.sellerIsMissing(reading.sellerId)) {
            val message = ErrorMessage("Seller ${reading.sellerId.value.value} not found.")
            throw SellerNotFoundException(message)
        }
    }

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<SellerVersionReading> { reading ->
        if (reading.reader.cannotReadPublicContent()) {
            val message = ErrorMessage("Only a visitor, a user or the system can read the versions of the seller.")
            throw SellerReadingForbiddenException(message)
        }
    }
}
