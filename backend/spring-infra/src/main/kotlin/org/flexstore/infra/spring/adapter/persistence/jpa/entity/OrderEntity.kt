package org.flexstore.infra.spring.adapter.persistence.jpa.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.math.BigDecimal
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

    // STANDARD or EXPRESS, and its cost in the currency of the lines
    @Column(name = "shipping_method", nullable = false, updatable = false)
    val shippingMethod: String,

    @Column(name = "shipping_cost", nullable = false, updatable = false)
    val shippingCost: BigDecimal,

    // A copy of the discount code entered and of the amount it took off; no code (null): no discount, 0
    @Column(name = "discount_code", updatable = false)
    val discountCode: String?,

    @Column(name = "discount_amount", nullable = false, updatable = false)
    val discountAmount: BigDecimal,

    // Addresses of the customer (rows of the address table, owned by UserEntity)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    val deliveryAddress: AddressEntity,

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "billing_address_id", nullable = false)
    val billingAddress: AddressEntity,

    // The last payment started for the order, none before
    @Column(name = "payment_id")
    val paymentId: String? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id", nullable = false)
    @OrderBy("position")
    val lines: List<OrderLineEntity> = emptyList(),

    // The statuses of the orders read are loaded in a single query
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_order_id", nullable = false)
    @OrderBy("position")
    @Fetch(FetchMode.SUBSELECT)
    val statusChanges: List<OrderStatusChangeEntity> = emptyList()
) : TimestampedEntity()
