package org.flexstore.infra.spring

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component open class DebugEnv(
    @Value("\${FLEXSTORE_DATASOURCE_HOST:NOT_FOUND}") private val host: String,
    @Value("\${FLEXSTORE_DATASOURCE_HOST_SUFFIX:NOT_FOUND}") private val hostSuffix: String,
    @Value("\${FLEXSTORE_DATASOURCE_PORT:NOT_FOUND}") private val port: String,
    @Value("\${FLEXSTORE_DATASOURCE_DATABASE:NOT_FOUND}") private val database: String
) {
    @PostConstruct
    fun printEnv() {
        println(">>> FLEXSTORE_DATASOURCE_HOST = $host")
        println(">>> FLEXSTORE_DATASOURCE_HOST_SUFFIX = $hostSuffix")
        println(">>> FLEXSTORE_DATASOURCE_PORT = $port")
        println(">>> FLEXSTORE_DATASOURCE_DATABASE = $database")
    }
}