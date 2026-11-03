package org.flexstore.infra.spring.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

// The cache of the reads of the user table: every update, delete and insert of a user empties it. The entries
// also expire, so that a row changed by something other than this application is read again in the end
const val USERS_CACHE = "users"

@Configuration
@EnableCaching
open class CacheConfig {

    @Bean
    open fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager(USERS_CACHE)
        val caffeine = Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(Duration.ofMinutes(10))
        cacheManager.setCaffeine(caffeine)
        return cacheManager
    }
}
