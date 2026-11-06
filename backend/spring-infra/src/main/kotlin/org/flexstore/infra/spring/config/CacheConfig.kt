package org.flexstore.infra.spring.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

// The cache of the reads of the user table, keyed by the id of the user: every insert, update and delete of a
// user evicts that id only. The entries also expire, so that a row changed by something other than this
// application is read again in the end
const val USERS_CACHE = "users"

// The cache of the read of the shop settings table, a single row: every save of the settings evicts it
const val SHOP_SETTINGS_CACHE = "shopSettings"

// The cache of the reads of the seller table: the current seller and the sellers read by id. Every save of a
// seller evicts the whole cache, because it changes which version is the current one. The entries also expire
const val SELLERS_CACHE = "sellers"

// The cache of the reads of the product table (the catalog, a product by id, a category): every save evicts it
const val PRODUCTS_CACHE = "products"

private const val MAXIMUM_CACHED_ENTRIES = 1_000L
private const val ENTRY_LIFETIME_MINUTES = 10L

@Configuration
@EnableCaching
open class CacheConfig {

    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager(USERS_CACHE, SHOP_SETTINGS_CACHE, SELLERS_CACHE, PRODUCTS_CACHE)
        val entryLifetime = Duration.ofMinutes(ENTRY_LIFETIME_MINUTES)
        val caffeine = Caffeine.newBuilder().maximumSize(MAXIMUM_CACHED_ENTRIES).expireAfterWrite(entryLifetime)
        cacheManager.setCaffeine(caffeine)
        return cacheManager
    }
}
