package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.security.asUser
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

// The addresses of the orders of the tests: delivered and billed to the same home
const val HOME_ADDRESSES = """{"deliveryAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
    "firstName": "Jane", "lastName": "Doe",
    "street": "12 rue des Lilas", "complement": "Bâtiment B",
    "postalCode": "75011", "city": "Paris", "country": "France"}}"""

fun MockHttpServletRequestBuilder.withHomeAddresses(): MockHttpServletRequestBuilder =
    contentType(MediaType.APPLICATION_JSON).content(HOME_ADDRESSES)

// The addresses of the tests, and the shipping method (STANDARD or EXPRESS)
fun MockHttpServletRequestBuilder.withHomeAddressesShipped(shippingMethod: String):
    MockHttpServletRequestBuilder {
    val addressesAndShipping = HOME_ADDRESSES.removeSuffix("}") + """, "shippingMethod": "$shippingMethod"}"""
    return contentType(MediaType.APPLICATION_JSON).content(addressesAndShipping)
}

// Without a Stripe key, the tests pay with the simulated payment provider: the order is paid at once
fun MockMvc.pay(orderId: String, customerId: String): ResultActions =
    perform(post("/api/orders/$orderId/payment").with(asUser(customerId)))

// Deletes the orders of a customer: their invoices first (an invoice keeps its order), then the orders, then
// their payments (an order refers to its last payment)
fun JdbcTemplate.deleteOrdersOf(customerId: String) {
    val paymentIds = queryForList(
        "SELECT payment_id FROM customer_order WHERE user_id = ? AND payment_id IS NOT NULL", String::class.java, customerId
    )
    update("DELETE FROM invoice WHERE customer_order_id IN (SELECT id FROM customer_order WHERE user_id = ?)", customerId)
    update("DELETE FROM customer_order WHERE user_id = ?", customerId)
    paymentIds.forEach { paymentId -> update("DELETE FROM payment WHERE id = ?", paymentId) }
}
