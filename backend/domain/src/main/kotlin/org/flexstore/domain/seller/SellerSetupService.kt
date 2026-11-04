package org.flexstore.domain.seller

import org.flexstore.domain.seller.usecase.SellerUseCases
import org.flexstore.domain.settings.ShopSettingsAdministrationService
import org.flexstore.domain.user.model.UserId

// Domain service: the setup assistant of the shop. An administrator gives the seller when the shop opens; once
// the seller is saved (so valid), the seller setup of the shop settings is turned on. A refused seller leaves
// the setup to be done
class SellerSetupService(
    private val sellerUseCases: SellerUseCases,
    private val shopSettingsAdministrationService: ShopSettingsAdministrationService
) {

    fun complete(editor: UserId, sellerDetail: SellerDetail): Seller {
        val seller = sellerUseCases.update(editor, sellerDetail)
        shopSettingsAdministrationService.completeSellerSetup(editor)
        return seller
    }
}
