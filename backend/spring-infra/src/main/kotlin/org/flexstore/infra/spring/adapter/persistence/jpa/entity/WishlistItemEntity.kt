package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A product a customer saved to their wishlist, to buy it back later
@Entity
@Table(name = "wishlist_item")
data class WishlistItemEntity(
    @Id
    val id: String,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: UserEntity,

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    val product: ProductEntity
) : TimestampedEntity()
