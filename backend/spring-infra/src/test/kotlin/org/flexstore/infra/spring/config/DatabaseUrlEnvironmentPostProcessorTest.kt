package org.flexstore.infra.spring.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.springframework.boot.SpringApplication
import org.springframework.mock.env.MockEnvironment
import kotlin.test.Test

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    fun `should convert a connection string to datasource properties`() {
        // Act
        val properties = datasourceProperties("postgresql://flex:p%40ss@dpg-abc-a.frankfurt-postgres.render.com:5432/flexstore")
        // Assert
        assertThat(properties).isEqualTo(
            mapOf(
                "spring.datasource.url" to "jdbc:postgresql://dpg-abc-a.frankfurt-postgres.render.com:5432/flexstore",
                "spring.datasource.username" to "flex",
                "spring.datasource.password" to "p@ss"
            )
        )
    }

    @Test
    fun `should convert a connection string without port`() {
        // Act
        val properties = datasourceProperties("postgres://flex:flex@dpg-abc-a/flexstore")
        // Assert
        assertThat(properties["spring.datasource.url"]).isEqualTo("jdbc:postgresql://dpg-abc-a/flexstore")
    }

    @Test
    fun `should keep a JDBC URL as is`() {
        // Act
        val properties = datasourceProperties("jdbc:postgresql://localhost:5432/flexstore?user=flex&password=flex")
        // Assert
        assertThat(properties).isEqualTo(mapOf("spring.datasource.url" to "jdbc:postgresql://localhost:5432/flexstore?user=flex&password=flex"))
    }

    @Test
    fun `should reject an unsupported URL`() {
        // Act
        val thrown = catchThrowable { datasourceProperties("mysql://flex:flex@localhost/flexstore") }
        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should override datasource properties when the variable is set`() {
        // Arrange
        val environment = MockEnvironment()
            .withProperty("spring.datasource.url", "jdbc:postgresql://\${FLEXSTORE_DATASOURCE_HOST}/db")
            .withProperty(DATABASE_URL_ENV, "postgresql://flex:flex@host:5432/flexstore")
        // Act
        DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, SpringApplication())
        // Assert
        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://host:5432/flexstore")
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("flex")
    }

    @Test
    fun `should not change anything when the variable is not set`() {
        // Arrange
        val environment = MockEnvironment().withProperty("spring.datasource.url", "jdbc:postgresql://localhost/flexstore")
        // Act
        DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(environment, SpringApplication())
        // Assert
        assertThat(environment.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://localhost/flexstore")
    }
}
