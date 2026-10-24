package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.settings.*
import com.fasterxml.jackson.annotation.JsonProperty

// orderConfirmationEmail: ENABLED or DISABLED
data class JsonShopSettings(@JsonProperty("orderConfirmationEmail") val orderConfirmationEmail: String) {
    constructor(settings: ShopSettings) : this(settings.orderConfirmationEmail.name)
}
