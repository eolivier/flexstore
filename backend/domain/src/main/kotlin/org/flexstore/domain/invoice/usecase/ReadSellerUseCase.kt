package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.ucop.domain.entity.*

// The shop that sells, as written on its invoices
class ReadSellerUseCase(private val sellerRepository: SellerRepository) : QueryUseCase<Unit, Seller> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, Seller> { sellerRepository.load() }

    override fun getPostConditions(): PostConditions<Seller> = PostConditions.none()
}
