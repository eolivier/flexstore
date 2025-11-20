package org.flexstore.infra.spring

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component open class DebugEnv(
    @Value("\${FLEXSTORE_DATABASE_URL:NOT_FOUND}") private val url: String
) {
    @PostConstruct
    fun printEnv() {
        println(">>> FLEXSTORE_DATABASE_URL = $url")
    }
}