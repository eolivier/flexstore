package org.flexstore.domain.sharedkernel

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.Instant

class InstantProviderTest : WithAssertions {

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    @Test
    fun `a fixed moment should stay the current moment`() {
        // Arrange
        val moment = Instant.parse("2026-10-24T10:00:00Z")
        InstantProvider.fixAt(moment)
        // Act
        val now = InstantProvider.now()
        // Assert
        assertThat(now).isEqualTo(moment)
    }

    @Test
    fun `once reset, the current moment should be the one of the system clock`() {
        // Arrange
        InstantProvider.fixAt(Instant.parse("2020-01-01T00:00:00Z"))
        InstantProvider.reset()
        // Act
        val now = InstantProvider.now()
        // Assert
        assertThat(now).isAfter(Instant.parse("2026-01-01T00:00:00Z"))
    }
}
