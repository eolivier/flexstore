package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

// A postal address of a user (a user may have several), in the order they were given
@Entity
@Table(name = "address")
data class AddressEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val position: Int,

    @Column(nullable = false)
    val street: String,

    val complement: String? = null,

    @Column(name = "postal_code", nullable = false)
    val postalCode: String,

    @Column(nullable = false)
    val city: String,

    @Column(nullable = false)
    val country: String
) : TimestampedEntity()
