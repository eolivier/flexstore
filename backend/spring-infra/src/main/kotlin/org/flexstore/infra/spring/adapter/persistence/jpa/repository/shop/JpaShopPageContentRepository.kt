package org.flexstore.infra.spring.adapter.persistence.jpa.repository.shop

import org.flexstore.domain.pagecontent.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopPageContentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaShopPageContentRepository : JpaRepository<ShopPageContentEntity, String> {

    fun findByPageType(pageType: String): ShopPageContentEntity
}
