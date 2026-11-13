package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SHOP_SETTINGS_ID
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopSettingsEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.domain.settings.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.config.SHOP_SETTINGS_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The single row of the shop settings, created by the migrations. Its read is cached; every save evicts it
@Repository
@Transactional
class PostgresShopSettingsRepositoryAdapter(private val jpaRepo: JpaShopSettingsRepository) :
    ShopSettingsRepository {

    @Cacheable(SHOP_SETTINGS_CACHE)
    @Transactional(readOnly = true)
    override fun load(): ShopSettings = jpaRepo.getReferenceById(SHOP_SETTINGS_ID).toDomain()

    @CacheEvict(SHOP_SETTINGS_CACHE, allEntries = true)
    override fun save(settings: ShopSettings): ShopSettings {
        val settingsEntity = ShopSettingsEntity(
            orderConfirmationEmail = settings.orderConfirmationEmail.name,
            paymentDelayMinutes = settings.paymentDelay.minutes,
            freeShippingThreshold = settings.freeShippingThreshold.price.amount.value,
            sellerSetup = settings.sellerSetup.name,
            abandonedCartReminder = settings.abandonedCartReminder.activation.name,
            abandonedCartDelayHours = settings.abandonedCartDelay.hours
        )
        return jpaRepo.save(settingsEntity).toDomain()
    }

    private fun ShopSettingsEntity.toDomain(): ShopSettings {
        val threshold = FreeShippingThreshold(Price(Amount(freeShippingThreshold), Currency.EUR))
        val orderRules = OrderRules(PaymentDelay(paymentDelayMinutes), threshold)
        val sellerSetupActivation = Activation.valueOf(sellerSetup)
        val abandonedCartReminder = AbandonedCartReminder(
            Activation.valueOf(abandonedCartReminder), AbandonedCartDelay(abandonedCartDelayHours)
        )
        return ShopSettings(
            Activation.valueOf(orderConfirmationEmail), orderRules, sellerSetupActivation, abandonedCartReminder
        )
    }
}
