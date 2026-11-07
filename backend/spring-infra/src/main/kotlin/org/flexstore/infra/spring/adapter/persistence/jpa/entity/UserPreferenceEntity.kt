package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The preferences of a customer: the display mode and the language they chose for the shop, each empty until
// the customer chooses it
@Entity
@Table(name = "user_preference")
data class UserPreferenceEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    val userId: String,

    // LIGHT or DARK
    @Column(name = "color_mode")
    val colorMode: String? = null,

    // EN or FR
    @Column(name = "language")
    val language: String? = null
) : TimestampedEntity()
