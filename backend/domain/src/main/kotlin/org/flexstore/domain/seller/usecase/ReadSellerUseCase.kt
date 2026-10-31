package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A version of the seller, as written on the invoices issued with it
class ReadSellerUseCase(private val sellerRepository: SellerRepository) : QueryUseCase<SellerId, Seller> {

    override fun getPreConditions(): PreConditions<SellerId> = PreConditions(sellerExistsCondition())

    override fun getNominalQuery() =
        NominalQuery<SellerId, Seller> { sellerId -> sellerRepository.findBy(sellerId)!! }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()

    private fun sellerExistsCondition() = PreCondition<SellerId> { sellerId ->
        if (sellerRepository.sellerIsMissing(sellerId)) {
            throw SellerNotFoundException(ErrorMessage("Seller ${sellerId.value.value} not found."))
        }
    }
}
