package org.flexstore.infra.spring

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value

class DebugEnv(
    @Value("\${FLEXSTORE_DATASOURCE_INTERNAL_HOST:NOT_FOUND}") private val host: String
) {
    @PostConstruct
    fun printEnv() {
        println(">>> FLEXSTORE_DATASOURCE_INTERNAL_HOST = $host")
    }
}