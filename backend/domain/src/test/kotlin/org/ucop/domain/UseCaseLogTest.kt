package org.ucop.domain

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.ucop.domain.entity.*

class UseCaseLogTest {

    private val logger = LoggerFactory.getLogger("UseCase") as Logger
    private val appender = ListAppender<ILoggingEvent>().apply { start(); logger.addAppender(this) }

    @AfterEach
    fun detachAppender() {
        logger.detachAppender(appender)
    }

    private fun loggedLine() = appender.list.single().let { it.level to it.formattedMessage }

    // A use case whose nominal scenario runs the given step
    private class SampleUseCase(private val step: (String) -> Unit) : UseCase<String> {
        override fun getPreConditions() = PreConditions.none<String>()
        override fun getNominalScenario() = NominalScenario(Step(step))
        override fun getPostConditions() = PostConditions.none<String>()
    }

    @Test
    fun `a use case done should log one info line with its name and duration`() {
        // Act
        SampleUseCase {}.unfold("request")
        // Assert
        val (level, message) = loggedLine()
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).matches("SampleUseCase: done \\(\\d+ ms\\)")
    }

    @Test
    fun `a refused use case should log one warning line with the reason`() {
        // Arrange
        val refused = SampleUseCase { throw NominalException(ErrorMessage("The cart is empty.")) }
        // Act
        val thrown = catchThrowable { refused.unfold("request") }
        // Assert
        val (level, message) = loggedLine()
        assertThat(thrown).isInstanceOf(NominalException::class.java)
        assertThat(level).isEqualTo(Level.WARN)
        assertThat(message).matches("SampleUseCase: refused \\(NominalException: The cart is empty\\.\\) \\(\\d+ ms\\)")
    }

    @Test
    fun `a failed use case should log one error line`() {
        // Arrange
        val failing = SampleUseCase { throw IllegalStateException("Database unreachable") }
        // Act
        val thrown = catchThrowable { failing.unfold("request") }
        // Assert
        val (level, message) = loggedLine()
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(level).isEqualTo(Level.ERROR)
        assertThat(message).startsWith("SampleUseCase: failed (IllegalStateException: Database unreachable)")
    }

    @Test
    fun `the request should never be logged`() {
        // Act
        SampleUseCase {}.unfold("secret-password")
        // Assert
        assertThat(loggedLine().second).doesNotContain("secret-password")
    }
}
