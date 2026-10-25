package org.flexstore.infra.spring.config

import org.flexstore.domain.image.ImageResizer
import org.flexstore.infra.spring.adapter.image.JavaImageResizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ImageConfig {

    @Bean
    open fun imageResizer(): ImageResizer = JavaImageResizer()
}
