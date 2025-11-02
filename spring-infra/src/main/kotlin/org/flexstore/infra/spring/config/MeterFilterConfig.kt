package org.flexstore.infra.spring.config

import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MeterFilterConfig {
    @Bean
    open fun commonTagsFilter(): MeterFilter = MeterFilter.commonTags(
        listOf(
            Tag.of("service", "flexstore"),
            Tag.of("env", System.getenv("APP_ENV") ?: "local")
        )
    )
}