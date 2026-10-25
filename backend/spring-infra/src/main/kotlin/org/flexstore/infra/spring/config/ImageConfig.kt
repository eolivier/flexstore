package org.flexstore.infra.spring.config

import org.flexstore.domain.image.ImageResizerRepository
import org.flexstore.infra.image.JavaImageResizerRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class ImageConfig {

    @Bean
    open fun imageResizerRepository(): ImageResizerRepository = JavaImageResizerRepository()
}
