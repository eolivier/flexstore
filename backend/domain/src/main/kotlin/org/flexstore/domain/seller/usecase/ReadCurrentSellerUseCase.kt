package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.ucop.domain.entity.*

// The current seller: the one the next invoices will be issued with
class ReadCurrentSellerUseCase(private val sellerRepository: SellerRepository) : QueryUseCase<Unit, Seller> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, Seller> { sellerRepository.current() }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()
}
