package org.flexstore.infra.spring.config

import org.flexstore.infra.spring.adapter.rest.TOTAL_COUNT_HEADER
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

// Only the front end of the development server calls the API from another origin: in production, the reverse
// proxy serves the shop and the API from the same address (see deploy/Caddyfile)
@Configuration
open class CorsConfig {
    @Bean
    open fun corsConfigurer(): WebMvcConfigurer = object : WebMvcConfigurer {
        override fun addCorsMappings(registry: CorsRegistry) {
            registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // The front end reads the number of items of a paged list in this header
                .exposedHeaders(TOTAL_COUNT_HEADER)
        }
    }
}