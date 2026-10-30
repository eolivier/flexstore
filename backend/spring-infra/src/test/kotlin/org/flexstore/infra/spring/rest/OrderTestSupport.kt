package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.security.asUser
import org.springframework.http.MediaType
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
