package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The display mode a customer chose for the shop
@Entity
@Table(name = "color_mode_preference")
data class ColorModePreferenceEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    // LIGHT or DARK
    @Column(name = "color_mode", nullable = false)
    val colorMode: String
) : TimestampedEntity()
