package org.flexstore.infra.spring.adapter.persistence.jpa.repository.shipping

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShippingMethodEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaShippingMethodRepository : JpaRepository<ShippingMethodEntity, String>
