package org.flexstore.domain.settings.usecase

import org.flexstore.domain.settings.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the settings of the shop, to change them. The journeys of the shop read them with
// ReadShopSettingsUseCase, without actor
class ReadAdministeredShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository
) : QueryUseCase<ShopSettingsReading, ShopSettings> {

    override fun getPreConditions(): PreConditions<ShopSettingsReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<ShopSettingsReading, ShopSettings> { shopSettingsRepository.load() }

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<ShopSettingsReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can read the settings of the shop.")
            throw ShopSettingsAdministrationForbiddenException(message)
        }
    }
}
