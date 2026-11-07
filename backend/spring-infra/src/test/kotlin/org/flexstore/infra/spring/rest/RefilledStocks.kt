package org.flexstore.infra.spring.rest

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

private val namespace = ExtensionContext.Namespace.create(RefilledStocks::class.java)
private const val STOCKS_BEFORE_THE_TEST = "stocksBeforeTheTest"
private const val GIVEN_STOCKS = "SELECT id, stock_quantity FROM product WHERE stock_quantity IS NOT NULL"

// The tests share the database of the local shop, and an order reserves its units. Before each test, the stocks
// are refilled, so that they never run out; after it, they are put back as they were, so that the tests leave
// the stocks of the local shop as they found them
class RefilledStocks : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val jdbcTemplate = jdbcTemplateOf(context)
        val stocksBeforeTheTest = jdbcTemplate.query(GIVEN_STOCKS) { row, _ ->
            arrayOf<Any>(row.getInt("stock_quantity"), row.getString("id"))
        }
        context.getStore(namespace).put(STOCKS_BEFORE_THE_TEST, stocksBeforeTheTest)
        jdbcTemplate.update("UPDATE product SET stock_quantity = 100 WHERE stock_quantity IS NOT NULL")
    }

    override fun afterEach(context: ExtensionContext) {
        val store = context.getStore(namespace)
        @Suppress("UNCHECKED_CAST")
        val stocksBeforeTheTest = store.remove(STOCKS_BEFORE_THE_TEST) as List<Array<Any>>
        val restoreStock = "UPDATE product SET stock_quantity = ? WHERE id = ?"
        jdbcTemplateOf(context).batchUpdate(restoreStock, stocksBeforeTheTest)
    }

    private fun jdbcTemplateOf(context: ExtensionContext) =
        SpringExtension.getApplicationContext(context).getBean(JdbcTemplate::class.java)
}
