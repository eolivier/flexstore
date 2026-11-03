package org.flexstore.domain.settings.usecase

import org.flexstore.domain.settings.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// An administrator reads the settings of the shop, to change them. The journeys of the shop read them with
// ReadShopSettingsUseCase, without actor
class ReadAdministeredShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository,
    private val userRepository: UserRepository
) : QueryUseCase<ShopSettingsReading, ShopSettings> {

    override fun getPreConditions(): PreConditions<ShopSettingsReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<ShopSettingsReading, ShopSettings> { shopSettingsRepository.load() }

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<ShopSettingsReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw ShopSettingsAdministrationForbiddenException(
                ErrorMessage("Only an administrator can read the settings of the shop.")
            )
        }
    }
}
