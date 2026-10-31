package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A version of the seller, never changed once saved: the current seller is the one with the highest version
@Entity
@Table(name = "seller")
data class SellerEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true, updatable = false)
    val version: Int,

    @Column(nullable = false, updatable = false)
    val name: String,

    @Column(nullable = false, updatable = false)
    val address: String,

    @Column(nullable = false, updatable = false)
    val siret: String,

    @Column(name = "vat_number", nullable = false, updatable = false)
    val vatNumber: String
) : TimestampedEntity()
