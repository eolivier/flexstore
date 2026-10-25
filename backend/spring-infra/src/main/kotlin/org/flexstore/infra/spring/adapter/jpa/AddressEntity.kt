package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

// A postal address: its recipient and where it is. It belongs to no user: the users (UserAddressEntity) and the
// orders (OrderEntity) refer to it
@Entity
@Table(name = "address")
data class AddressEntity(
    @Id
    val id: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(name = "company_name")
    val companyName: String? = null,

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
