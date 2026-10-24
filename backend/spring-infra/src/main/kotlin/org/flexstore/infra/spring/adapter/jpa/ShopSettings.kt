package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

const val SHOP_SETTINGS_ID = 1

@Entity
@Table(name = "shop_settings")
data class ShopSettingsEntity(
    @Id
    val id: Int = SHOP_SETTINGS_ID,

    @Column(name = "order_confirmation_email", nullable = false)
    val orderConfirmationEmail: String
) : TimestampedEntity()
