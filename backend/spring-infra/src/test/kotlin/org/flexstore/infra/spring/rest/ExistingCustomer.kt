package org.flexstore.infra.spring.rest

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

// A customer who exists and whose account is active: the token of a user who does not exist, or whose account is
// refused, is turned into an anonymous request before the routes are authorized
const val EXISTING_CUSTOMER_ID = "it-existing-customer"

private const val INSERT_CUSTOMER = """INSERT INTO "user" (id, first_name, last_name, email, password, role, status)
    VALUES (?, 'Jane', 'Doe', 'it-existing-customer@example.com', 'not-used', 'USER', 'ACTIVE')"""
private const val DELETE_CUSTOMER = """DELETE FROM "user" WHERE id = ?"""

// Created before each test and removed after it, so that the tests leave the users as they found them
class ExistingCustomer : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val jdbcTemplate = jdbcTemplateOf(context)
        jdbcTemplate.update(DELETE_CUSTOMER, EXISTING_CUSTOMER_ID)
        jdbcTemplate.update(INSERT_CUSTOMER, EXISTING_CUSTOMER_ID)
    }

    override fun afterEach(context: ExtensionContext) {
        jdbcTemplateOf(context).update(DELETE_CUSTOMER, EXISTING_CUSTOMER_ID)
    }

    private fun jdbcTemplateOf(context: ExtensionContext) =
        SpringExtension.getApplicationContext(context).getBean(JdbcTemplate::class.java)
}
