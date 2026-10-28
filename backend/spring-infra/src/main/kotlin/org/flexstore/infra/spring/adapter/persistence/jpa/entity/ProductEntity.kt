package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.math.BigDecimal

// A product of the catalog, with its SKU: its name and its description are in its translations, one per
// language, the default one included
@Entity
@Table(name = "product")
data class ProductEntity(
    @Id
    val id: String,

    @Column(nullable = false, unique = true)
    val sku: String,

    @Column(nullable = false)
    val category: String,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(nullable = false)
    val currency: String,

    // The translations of all the products read are loaded in a single query
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    val translations: List<ProductTranslationEntity> = emptyList()
) : TimestampedEntity()
