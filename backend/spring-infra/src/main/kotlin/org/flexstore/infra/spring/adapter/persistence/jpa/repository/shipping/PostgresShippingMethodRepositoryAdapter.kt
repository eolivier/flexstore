package org.flexstore.infra.spring.adapter.persistence.jpa.repository.shipping

import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The shipping methods, kept in PostgreSQL; the row of a method keeps its creation date when it is saved again
@Repository
@Transactional
class PostgresShippingMethodRepositoryAdapter(
    private val jpaRepo: JpaShippingMethodRepository
) : ShippingMethodRepository {

    override fun save(method: ShippingMethod) {
        val entity = method.toEntity()
        jpaRepo.findById(entity.id).ifPresent { stored -> entity.createdAt = stored.createdAt }
        jpaRepo.save(entity)
    }

    @Transactional(readOnly = true)
    override fun findBy(shippingMethodId: ShippingMethodId): ShippingMethod? =
        jpaRepo.findById(shippingMethodId.id.value).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun findAll() = ShippingMethods(jpaRepo.findAll(CHEAPEST_FIRST).map { it.toDomain() })
}

private val CHEAPEST_FIRST = Sort.by(Sort.Order.asc("cost"), Sort.Order.asc("label"))
