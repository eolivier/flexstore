package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.seller.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SellerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaSellerRepository : JpaRepository<SellerEntity, String> {

    fun findFirstByOrderByVersionDesc(): SellerEntity
}
