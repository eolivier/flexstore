package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.DiscountCodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaDiscountCodeRepository : JpaRepository<DiscountCodeEntity, String> {
    fun findByCode(code: String): DiscountCodeEntity?
}
