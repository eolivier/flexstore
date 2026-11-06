package org.flexstore.infra.spring

import org.flexstore.infra.spring.config.PRODUCTS_CACHE
import org.springframework.cache.CacheManager

// The tests that delete products through the Spring Data repository bypass the adapter, which keeps the cached
// catalog up to date: they empty the cache so that the next read comes from the table
fun CacheManager.clearProductCache() {
    getCache(PRODUCTS_CACHE)?.clear()
}
