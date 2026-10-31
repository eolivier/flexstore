package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*

// The use cases of the seller edited by the administrators: the current one is read, and replaced by a new
// version (read back once saved). The invoices already issued keep their own version
class SellerUseCases(
    private val readCurrentSellerUseCase: ReadCurrentSellerUseCase,
    private val updateSellerUseCase: UpdateSellerUseCase
) {
    fun currentSeller() = readCurrentSellerUseCase.unfold(Unit)

    fun update(sellerDetails: SellerDetails): Seller {
        val seller = Seller(SellerId.nextId(), sellerDetails)
        updateSellerUseCase.unfold(seller)
        return readCurrentSellerUseCase.unfold(Unit)
    }
}
