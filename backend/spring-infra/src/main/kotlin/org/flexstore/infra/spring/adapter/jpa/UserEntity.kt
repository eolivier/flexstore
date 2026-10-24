package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*

@Entity
@Table(name = "\"user\"") // entre guillemets car "user" est mot réservé
data class UserEntity(
    @Id
    val id: String,

    @Column(name = "first_name", nullable = false)
    val firstName: String,

    @Column(name = "last_name", nullable = false)
    val lastName: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val role: String = "USER",

    @Column(nullable = false)
    val status: String = "ACTIVE",

    // The contact details given by a customer who creates their account
    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    val street: String? = null,

    @Column(name = "address_complement")
    val addressComplement: String? = null,

    @Column(name = "postal_code")
    val postalCode: String? = null,

    val city: String? = null,

    val country: String? = null
) : TimestampedEntity()
