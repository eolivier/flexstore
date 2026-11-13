package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopSettingsEntity
import org.flexstore.domain.settings.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaShopSettingsRepository : JpaRepository<ShopSettingsEntity, Int>
