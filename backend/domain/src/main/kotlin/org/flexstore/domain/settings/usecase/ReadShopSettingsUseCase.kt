package org.flexstore.domain.settings.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
import org.flexstore.domain.settings.*
import org.ucop.domain.entity.*

// The settings of the shop
class ReadShopSettingsUseCase(
    private val shopSettingsRepository: ShopSettingsRepository
) : QueryUseCase<Actor, ShopSettings> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<Actor, ShopSettings> { shopSettingsRepository.load() }

    override fun getPostConditions(): PostConditions<ShopSettings> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        if (reader.isNotAtLeastVisitor()) {
            throw ShopSettingsReadingForbiddenException(ErrorMessage("Only a visitor, a user or the system can read the settings of the shop."))
        }
    }
}
