package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import org.flexstore.domain.order.*

// An order as the administrators see it: the fields of the order, and the customer who placed it
data class JsonAdminOrder(
    @get:JsonUnwrapped val order: JsonOrder,
    @JsonProperty("customer") val customer: JsonOrderCustomer
) {
    constructor(administeredOrder: AdministeredOrder) :
        this(JsonOrder(administeredOrder.order), JsonOrderCustomer(administeredOrder.customer))
}

// The customer who placed an order: empty fields when their account cannot be read any more
data class JsonOrderCustomer(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String
) {
    constructor(orderCustomer: OrderCustomer) : this(
        orderCustomer.fullName.firstName.value,
        orderCustomer.fullName.lastName.value,
        orderCustomer.email.value
    )
}
