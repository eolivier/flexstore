package org.flexstore.domain.sharedkernel

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PageTest : WithAssertions {

    @ParameterizedTest(name = "a page size of {0} is brought back to {1}")
    @CsvSource("-5,1", "0,1", "1,1", "20,20", "100,100", "101,100", "5000,100")
    fun `a page size should be brought back to the nearest limit`(requested: Int, expected: Int) {
        // Act
        val pageSize = PageSize.clamped(requested)
        // Assert
        assertThat(pageSize).isEqualTo(PageSize(expected))
    }

    @ParameterizedTest(name = "a page number of {0} is brought back to {1}")
    @CsvSource("-3,0", "0,0", "1,1", "42,42")
    fun `a negative page number should be brought back to the first page`(requested: Int, expected: Int) {
        // Act
        val pageNumber = PageNumber.of(requested)
        // Assert
        assertThat(pageNumber).isEqualTo(PageNumber(expected))
    }

    @Test
    fun `the first page should hold the default number of items`() {
        // Act
        val pageRequest = PageRequest.first()
        // Assert
        assertThat(pageRequest).isEqualTo(PageRequest(PageNumber(0), PageSize(20)))
    }

    @Test
    fun `a page asked with values out of the limits should be brought back to them`() {
        // Act
        val pageRequest = PageRequest.of(-1, 1000)
        // Assert
        assertThat(pageRequest).isEqualTo(PageRequest(PageNumber(0), PageSize(100)))
    }
}
