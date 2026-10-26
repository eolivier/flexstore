package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The image of a product in one format (LARGE or SMALL), as bytes
@Entity
@Table(name = "product_image")
data class ProductImageEntity(
    @Id
    val id: String,

    @Column(name = "product_id", nullable = false, updatable = false)
    val productId: String,

    @Column(nullable = false, updatable = false)
    val format: String,

    @Column(name = "media_type", nullable = false)
    val mediaType: String,

    @Column(nullable = false)
    val content: ByteArray
) : TimestampedEntity()
