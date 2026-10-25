package org.flexstore.infra.spring.config

import org.flexstore.domain.sharedkernel.ShopUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The address of the shop (FLEXSTORE_FRONT_URL), where the links of the emails and the payment lead
@Configuration
open class ShopConfig {

    @Bean
    open fun shopUrl(@Value("\${flexstore.front-url}") frontUrl: String) = ShopUrl(frontUrl)
}
