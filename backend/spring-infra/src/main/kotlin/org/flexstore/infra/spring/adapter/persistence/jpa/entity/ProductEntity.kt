package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.math.BigDecimal
import java.time.Instant

// A product of the catalog, with its SKU and its stock: its name and its description are in its translations,
// one per language, the default one included
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

    // STANDARD, INTERMEDIATE, REDUCED or SUPER_REDUCED
    @Column(name = "vat_rate", nullable = false)
    val vatRate: String,

    // The promotion of the product: the percentage off the price, from a moment to a later one; all null when
    // the product is not on promotion
    @Column(name = "promotion_percentage")
    val promotionPercentage: BigDecimal? = null,

    @Column(name = "promotion_starts_at")
    val promotionStartsAt: Instant? = null,

    @Column(name = "promotion_ends_at")
    val promotionEndsAt: Instant? = null,

    // The stock of the product: the number of units, empty while it was never given, and the number at or under
    // which the administrators are warned. Written by the stock adapter only (updatable = false): saving the
    // product never overwrites them
    @Column(name = "stock_quantity", updatable = false)
    val stockQuantity: Int? = null,

    @Column(name = "stock_alert_threshold", nullable = false, updatable = false)
    val stockAlertThreshold: Int = 5,

    // The translations of all the products read are loaded in a single query
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    val translations: List<ProductTranslationEntity> = emptyList()
) : TimestampedEntity()
