package org.flexstore.infra.spring.adapter.persistence.jpa.repository.shop

import org.flexstore.domain.pagecontent.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.ShopPageContentEntity
import org.flexstore.infra.spring.config.SHOP_PAGE_CONTENT_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The contents of the shop pages, one row for each type, created by the migrations. The read of all of them is
// cached; every save evicts it
@Repository
@Transactional
class PostgresShopPageContentRepositoryAdapter(private val jpaRepo: JpaShopPageContentRepository) :
    ShopPageContentRepository {

    @Cacheable(SHOP_PAGE_CONTENT_CACHE)
    @Transactional(readOnly = true)
    override fun all() = ShopPageContents(jpaRepo.findAll().map { it.toDomain() })

    @CacheEvict(SHOP_PAGE_CONTENT_CACHE, allEntries = true)
    override fun save(content: ShopPageContent) {
        val storedContent = jpaRepo.findByPageType(content.type.name)
        jpaRepo.save(storedContent.copy(content = content.text.value))
    }

    private fun ShopPageContentEntity.toDomain() =
        ShopPageContent(ShopPageContentType.valueOf(pageType), ShopPageContentText(content))
}
