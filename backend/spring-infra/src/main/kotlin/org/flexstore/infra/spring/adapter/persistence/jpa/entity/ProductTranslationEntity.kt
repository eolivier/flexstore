package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The name and the description of a product in another language than the default one (en or fr)
@Entity
@Table(name = "product_translation")
data class ProductTranslationEntity(
    @Id
    val id: String,

    @Column(nullable = false, updatable = false)
    val language: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String
) : TimestampedEntity()
