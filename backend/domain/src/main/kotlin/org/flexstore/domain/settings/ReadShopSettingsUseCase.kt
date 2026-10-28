package org.flexstore.domain.settings

import org.ucop.domain.entity.*

// The settings of the shop
class ReadShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository
) : QueryUseCase<Unit, ShopSettings> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, ShopSettings> { shopSettingsRepository.load() }

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions.none()
}
