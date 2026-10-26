package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// A postal address: its recipient and where it is. It belongs to no user: the users (UserAddressEntity) and the
// orders (OrderEntity) refer to it
@Entity
@Table(name = "address")
data class AddressEntity(
    @Id
    val id: String,

    // PRIVATE_INDIVIDUAL (first name and last name) or LEGAL_ENTITY (company name, VAT number when given)
    @Column(name = "recipient_type", nullable = false)
    val recipientType: String,

    @Column(name = "first_name")
    val firstName: String? = null,

    @Column(name = "last_name")
    val lastName: String? = null,

    @Column(name = "company_name")
    val companyName: String? = null,

    @Column(name = "vat_number")
    val vatNumber: String? = null,

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
