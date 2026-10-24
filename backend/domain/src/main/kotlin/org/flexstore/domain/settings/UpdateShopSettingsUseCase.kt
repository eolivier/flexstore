package org.flexstore.domain.settings

import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*

class UpdateShopSettingsUseCase(private val shopSettingsRepository: ShopSettingsRepository) : UseCase<ShopSettings> {

    override fun getPreConditions(): List<PreCondition<ShopSettings>> = listOf(EmptyPreCondition())

    override fun getNominalScenario(): NominalScenario<ShopSettings> =
        NominalScenario(listOf(Step { settings -> shopSettingsRepository.save(settings) }))

    override fun getPostConditions(): List<PostCondition<ShopSettings>> = listOf(settingsAreSavedCondition())

    private fun settingsAreSavedCondition() = PostCondition<ShopSettings> { settings ->
        if (shopSettingsRepository.load() != settings) {
            throw ShopSettingsUpdateFailed(NonEmptyString("The shop settings were not saved."))
        }
    }
}
