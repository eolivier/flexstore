package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

@Entity
@Table(name = "cart")
data class CartEntity(
    @Id
    val id: String,

    // The customer of the cart; null for a visitor cart, whose id is kept by the client
    @Column(name = "user_id", unique = true)
    val userId: String? = null
) : TimestampedEntity()
