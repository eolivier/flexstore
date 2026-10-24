package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "product")
data class ProductEntity(
    @Id
    val id: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val category: String,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    val currency: String
) : TimestampedEntity()
