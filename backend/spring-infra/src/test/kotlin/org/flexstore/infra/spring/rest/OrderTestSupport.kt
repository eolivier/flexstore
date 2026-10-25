package org.flexstore.infra.spring.rest

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

// The addresses of the orders of the tests: delivered and billed to the same home
const val HOME_ADDRESSES = """{"deliveryAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
    "firstName": "Jane", "lastName": "Doe",
    "street": "12 rue des Lilas", "complement": "Bâtiment B",
    "postalCode": "75011", "city": "Paris", "country": "France"}}"""

fun MockHttpServletRequestBuilder.withHomeAddresses(): MockHttpServletRequestBuilder =
    contentType(MediaType.APPLICATION_JSON).content(HOME_ADDRESSES)
