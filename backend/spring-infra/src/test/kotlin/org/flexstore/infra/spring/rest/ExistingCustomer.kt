package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.config.USERS_CACHE
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

// A customer who exists and whose account is active: the token of a user who does not exist, or whose account is
// refused, is turned into an anonymous request before the routes are authorized
const val EXISTING_CUSTOMER_ID = "it-existing-customer"

private const val INSERT_CUSTOMER = """INSERT INTO "user"
    (id, first_name, last_name, email, password, role, status)
    VALUES (?, 'Jane', 'Doe', 'it-existing-customer@example.com', 'not-used', 'USER', 'ACTIVE')"""
private const val DELETE_CUSTOMER = """DELETE FROM "user" WHERE id = ?"""

// Created before each test and removed after it, so that the tests leave the users as they found them. Written
// directly in the database, so the cache of the reads of the user table (see CacheConfig) is evicted by hand
class ExistingCustomer : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        val jdbcTemplate = jdbcTemplateOf(applicationContext)
        jdbcTemplate.update(DELETE_CUSTOMER, EXISTING_CUSTOMER_ID)
        jdbcTemplate.update(INSERT_CUSTOMER, EXISTING_CUSTOMER_ID)
        evictCustomerFromCacheIn(applicationContext)
    }

    override fun afterEach(context: ExtensionContext) {
        val applicationContext = SpringExtension.getApplicationContext(context)
        jdbcTemplateOf(applicationContext).update(DELETE_CUSTOMER, EXISTING_CUSTOMER_ID)
        evictCustomerFromCacheIn(applicationContext)
    }

    private fun jdbcTemplateOf(applicationContext: ApplicationContext) =
        applicationContext.getBean(JdbcTemplate::class.java)

    private fun evictCustomerFromCacheIn(applicationContext: ApplicationContext) {
        applicationContext.getBean(CacheManager::class.java).getCache(USERS_CACHE)?.evict(EXISTING_CUSTOMER_ID)
    }
}
