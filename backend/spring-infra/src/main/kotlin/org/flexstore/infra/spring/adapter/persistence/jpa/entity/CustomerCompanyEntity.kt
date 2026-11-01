package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*

// The company a customer buys for: its name, its SIREN, and its intra-community VAT number (null: not given)
@Entity
@Table(name = "customer_company")
data class CustomerCompanyEntity(
    @Id
    val id: String,

    @Column(name = "company_name", nullable = false)
    val companyName: String,

    @Column(nullable = false)
    val siren: String,

    @Column(name = "vat_number")
    val vatNumber: String?
) : TimestampedEntity()
