package org.flexstore.infra.spring.adapter.persistence.jpa.entity

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

    // The phone number and the postal addresses given by a customer who creates their account
    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    // Null while disabled; set once an enrollment starts (status PENDING), kept once confirmed (status ENABLED)
    @Column(name = "two_factor_secret")
    val twoFactorSecret: String? = null,

    @Column(name = "two_factor_status")
    val twoFactorStatus: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @OrderBy("position")
    val addresses: List<UserAddressEntity> = emptyList(),

    // The company of a customer who buys for it: its name and its SIREN, null together when the customer buys
    // for themselves, and its intra-community VAT number (null: not given)
    @Column(name = "company_name")
    val companyName: String? = null,

    @Column(name = "company_siren")
    val companySiren: String? = null,

    @Column(name = "company_vat_number")
    val companyVatNumber: String? = null,

    // The failed sign-ins in a row, 0 for none. Written by the failed sign-in adapter only (updatable = false):
    // saving the user never overwrites it
    @Column(name = "failed_sign_in_count", nullable = false, updatable = false)
    val failedSignInCount: Int = 0
) : TimestampedEntity()
