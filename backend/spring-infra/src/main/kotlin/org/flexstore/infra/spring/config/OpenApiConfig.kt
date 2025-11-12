package org.flexstore.infra.spring.config

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfig {
    @Bean
    open fun api(): OpenAPI = OpenAPI().info(
        Info()
            .title("Flexstore API")
            .description("REST contracts for Flexstore (hexagonal architecture online store sample project)")
            .version("v0.1.0")
            .contact(Contact().name("Emmanuel Olivier").url("https://github.com/eolivier"))
    )
}
