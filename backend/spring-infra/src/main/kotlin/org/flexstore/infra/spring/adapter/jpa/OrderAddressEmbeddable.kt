package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.Embeddable

// A copy of an address kept by an order (delivery or billing): its columns are prefixed in OrderEntity
@Embeddable
data class OrderAddressEmbeddable(
    val street: String,
    val complement: String? = null,
    val postalCode: String,
    val city: String,
    val country: String
)
