package org.flexstore.domain.settings

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

class UpdateShopSettingsUseCase(private val shopSettingsRepository: ShopSettingsRepository) : UseCase<ShopSettings> {

    override fun getPreConditions(): PreConditions<ShopSettings> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<ShopSettings> =
        NominalScenario(Step { settings -> shopSettingsRepository.save(settings) })

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions(settingsAreSavedCondition())

    private fun settingsAreSavedCondition() = PostCondition<ShopSettings> { settings ->
        if (shopSettingsRepository.load() != settings) {
            throw ShopSettingsUpdateFailed(ErrorMessage("The shop settings were not saved."))
        }
    }
}
