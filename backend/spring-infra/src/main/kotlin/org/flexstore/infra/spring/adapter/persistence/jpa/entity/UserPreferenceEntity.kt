package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The preferences of a customer: the display mode they chose for the shop
@Entity
@Table(name = "user_preference")
data class UserPreferenceEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    // LIGHT or DARK
    @Column(name = "color_mode", nullable = false)
    val colorMode: String
) : TimestampedEntity()
