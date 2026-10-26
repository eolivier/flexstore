package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// An address of a user (a user may have several), in the order they were given
@Entity
@Table(name = "user_address")
data class UserAddressEntity(
    @Id
    val id: String,

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE], fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    val address: AddressEntity,

    @Column(nullable = false)
    val position: Int
) : TimestampedEntity()
