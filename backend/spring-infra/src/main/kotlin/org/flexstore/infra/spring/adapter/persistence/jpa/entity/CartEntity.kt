package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import java.time.Instant
import org.flexstore.domain.sharedkernel.InstantProvider

@Entity
@Table(name = "cart")
data class CartEntity(
    @Id
    val id: String,

    // The customer of the cart; null for a visitor cart, whose id is kept by the client
    @Column(name = "user_id", unique = true)
    val userId: String? = null,

    // The moment the items of the cart last changed; the cart is abandoned when it is left unchanged for a while
    @Column(name = "last_changed_at", nullable = false)
    val lastChangedAt: Instant = InstantProvider.now(),

    // The moment the customer was reminded of the cart; null until then, and again after each change of the cart
    @Column(name = "reminded_at")
    val remindedAt: Instant? = null
) : TimestampedEntity()
