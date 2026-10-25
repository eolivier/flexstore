package org.flexstore.infra.spring.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

const val DATABASE_URL_ENV = "FLEXSTORE_DATABASE_URL"

/**
 * Configures the datasource from FLEXSTORE_DATABASE_URL when it is set (e.g. on Render).
 * Accepts a JDBC URL (jdbc:postgresql://...) or a connection string (postgres(ql)://user:password@host:port/db).
 * Without it, the FLEXSTORE_DATASOURCE_* variables of application.yml are used.
 */
open class DatabaseUrlEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val databaseUrl = environment.getProperty(DATABASE_URL_ENV)?.trim()
        if (databaseUrl.isNullOrEmpty()) {
            return
        }
        val datasourcePropertySource = MapPropertySource(DATABASE_URL_ENV, datasourceProperties(databaseUrl))
        environment.propertySources.addFirst(datasourcePropertySource)
    }
}

fun datasourceProperties(databaseUrl: String): Map<String, Any> {
    if (databaseUrl.startsWith("jdbc:")) {
        return mapOf("spring.datasource.url" to databaseUrl)
    }
    val uri = URI(databaseUrl)
    require(uri.scheme == "postgres" || uri.scheme == "postgresql") {
        "$DATABASE_URL_ENV must start with jdbc:postgresql://, postgres:// or postgresql://"
    }
    val port = if (uri.port == -1) "" else ":${uri.port}"
    val query = uri.rawQuery?.let { "?$it" } ?: ""
    val properties = mutableMapOf<String, Any>("spring.datasource.url" to "jdbc:postgresql://${uri.host}$port${uri.rawPath}$query")
    uri.rawUserInfo?.split(":", limit = 2)?.let { userInfo ->
        properties["spring.datasource.username"] = decode(userInfo[0])
        userInfo.getOrNull(1)?.let { properties["spring.datasource.password"] = decode(it) }
    }
    return properties
}

private fun decode(value: String) = URLDecoder.decode(value, StandardCharsets.UTF_8)
