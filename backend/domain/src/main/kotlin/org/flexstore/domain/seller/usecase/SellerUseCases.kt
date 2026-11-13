package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.ucop.domain.entity.Actor

// The use cases of the seller edited by the administrators: the current one is read, and replaced by a new
// version (read back once saved). The invoices already issued keep their own version. Everyone reads the social
// networks of the current one
class SellerUseCases(
    private val readCurrentSellerUseCase: ReadCurrentSellerUseCase,
    private val updateSellerUseCase: UpdateSellerUseCase,
    private val readSocialNetworksUseCase: ReadSocialNetworksUseCase
) {
    fun socialNetworks(reader: Actor) = readSocialNetworksUseCase.unfold(SocialNetworksReading(reader))

    fun currentSeller(reader: Actor) = readCurrentSellerUseCase.unfold(SellerReading(reader))

    fun update(editor: Actor, sellerDetail: SellerDetail): Seller {
        val seller = Seller(SellerId.nextId(), sellerDetail)
        updateSellerUseCase.unfold(SellerUpdate(editor, seller))
        return readCurrentSellerUseCase.unfold(SellerReading(editor))
    }
}
