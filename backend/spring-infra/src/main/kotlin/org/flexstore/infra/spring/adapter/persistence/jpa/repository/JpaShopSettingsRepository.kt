package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SHOP_SETTINGS_ID
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopSettingsEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.domain.settings.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaShopSettingsRepository : JpaRepository<ShopSettingsEntity, Int>

// The single row of the shop settings, created by the migrations
@Repository
@Transactional
class PostgresShopSettingsRepositoryAdapter(private val jpaRepo: JpaShopSettingsRepository) :
    ShopSettingsRepository {

    @Transactional(readOnly = true)
    override fun load(): ShopSettings = jpaRepo.getReferenceById(SHOP_SETTINGS_ID).toDomain()

    override fun save(settings: ShopSettings): ShopSettings {
        val settingsEntity = ShopSettingsEntity(
            orderConfirmationEmail = settings.orderConfirmationEmail.name,
            paymentDelayMinutes = settings.paymentDelay.minutes
        )
        return jpaRepo.save(settingsEntity).toDomain()
    }

    private fun ShopSettingsEntity.toDomain() =
        ShopSettings(Activation.valueOf(orderConfirmationEmail), PaymentDelay(paymentDelayMinutes))
}
