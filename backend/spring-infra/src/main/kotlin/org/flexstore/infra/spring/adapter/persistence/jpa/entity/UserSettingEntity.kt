package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant

// A setting a customer chose: its name (see UserSettingName) and its value, at most one per customer and name
@Entity
@Table(name = "user_setting")
data class UserSettingEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(nullable = false, updatable = false)
    val name: String,

    // LIGHT or DARK for color_mode, EN or FR for language, SHOWN or HIDDEN for guided_tour,
    // ACCEPTED or REFUSED for cookie_choice
    @Column(nullable = false)
    val value: String,

    // Only for cookie_choice: when the customer is asked again
    @Column(name = "expires_at")
    val expiresAt: Instant? = null
) : TimestampedEntity()
