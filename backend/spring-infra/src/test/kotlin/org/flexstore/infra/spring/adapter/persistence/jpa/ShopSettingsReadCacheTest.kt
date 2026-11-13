package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.settings.ShopSettings
import org.flexstore.domain.settings.ShopSettingsRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SHOP_SETTINGS_ID
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopSettingsEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.shop.JpaShopSettingsRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.shop.PostgresShopSettingsRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.math.BigDecimal
import java.util.function.Supplier

// The cache of the read of the shop settings table, with the real adapter behind the Spring cache, without a
// database
class ShopSettingsReadCacheTest {

    private val settingsEntity = ShopSettingsEntity(
        orderConfirmationEmail = "ENABLED", paymentDelayMinutes = 30,
        freeShippingThreshold = BigDecimal("50.00"), sellerSetup = "DISABLED",
        abandonedCartReminder = "DISABLED", abandonedCartDelayHours = 24
    )

    private val jpaShopSettingsRepository = mock(JpaShopSettingsRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var shopSettingsRepository: ShopSettingsRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaShopSettingsRepository.getReferenceById(SHOP_SETTINGS_ID)).thenReturn(settingsEntity)
        `when`(jpaShopSettingsRepository.save(any(ShopSettingsEntity::class.java))).thenReturn(settingsEntity)
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresShopSettingsRepositoryAdapter::class.java)
        context.registerBean(JpaShopSettingsRepository::class.java, Supplier { jpaShopSettingsRepository })
        context.refresh()
        shopSettingsRepository = context.getBean(ShopSettingsRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `reading the settings twice should read the table once`() {
        // Act
        shopSettingsRepository.load()
        shopSettingsRepository.load()
        // Assert
        verify(jpaShopSettingsRepository, times(1)).getReferenceById(SHOP_SETTINGS_ID)
    }

    @Test
    fun `an update should evict the cached settings`() {
        // Arrange
        val settings: ShopSettings = shopSettingsRepository.load()
        shopSettingsRepository.save(settings)
        clearInvocations(jpaShopSettingsRepository)
        // Act
        shopSettingsRepository.load()
        // Assert
        verify(jpaShopSettingsRepository, times(1)).getReferenceById(SHOP_SETTINGS_ID)
    }
}
