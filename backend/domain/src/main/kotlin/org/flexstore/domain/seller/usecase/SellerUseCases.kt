package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*

// The use cases of the seller edited by the administrators: the current one is read, and replaced by a new
// version (read back once saved). The invoices already issued keep their own version
class SellerUseCases(
    private val readCurrentSellerUseCase: ReadCurrentSellerUseCase,
    private val updateSellerUseCase: UpdateSellerUseCase
) {
    fun currentSeller() = readCurrentSellerUseCase.unfold(Unit)

    fun update(sellerDetail: SellerDetail): Seller {
        val seller = Seller(SellerId.nextId(), sellerDetail)
        updateSellerUseCase.unfold(seller)
        return readCurrentSellerUseCase.unfold(Unit)
    }
}
