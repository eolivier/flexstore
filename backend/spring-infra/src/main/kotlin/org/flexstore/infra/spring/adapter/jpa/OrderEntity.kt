package org.flexstore.infra.spring.adapter.jpa

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "customer_order")
data class OrderEntity(
    @Id
    val id: String,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: String,

    @Column(nullable = false)
    val status: String,

    @Column(name = "placed_at", nullable = false, updatable = false)
    val placedAt: Instant,

    // null for the orders placed before the addresses were asked for
    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "delivery_street")),
        AttributeOverride(name = "complement", column = Column(name = "delivery_complement")),
        AttributeOverride(name = "postalCode", column = Column(name = "delivery_postal_code")),
        AttributeOverride(name = "city", column = Column(name = "delivery_city")),
        AttributeOverride(name = "country", column = Column(name = "delivery_country"))
    )
    val deliveryAddress: OrderAddressEmbeddable? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "street", column = Column(name = "billing_street")),
        AttributeOverride(name = "complement", column = Column(name = "billing_complement")),
        AttributeOverride(name = "postalCode", column = Column(name = "billing_postal_code")),
        AttributeOverride(name = "city", column = Column(name = "billing_city")),
        AttributeOverride(name = "country", column = Column(name = "billing_country"))
    )
    val billingAddress: OrderAddressEmbeddable? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("position")
    val lines: List<OrderLineEntity> = emptyList()
) : TimestampedEntity()
