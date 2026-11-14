package org.flexstore.infra.spring.adapter.persistence.jpa.repository.shipping

import org.flexstore.domain.order.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShippingMethodEntity

fun ShippingMethodEntity.toDomain() = ShippingMethod(
    ShippingMethodId(Identity(id)),
    ShippingLabel(label),
    Price(Amount(cost), Currency.EUR),
    FreeShippingEligibility.valueOf(freeShippingEligibility),
    ShippingMethodStatus.valueOf(status)
)

fun ShippingMethod.toEntity() = ShippingMethodEntity(
    id.id.value,
    label().value,
    cost.amount.value,
    eligibility.name,
    status.name
)
