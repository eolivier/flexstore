package org.flexstore.domain.user.model

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FullNameTest : WithAssertions {

    @ParameterizedTest(name = "\"{0}\" + \"{1}\" -> \"{2}\"")
    @CsvSource(
        "Jane, Doe, Jane Doe",
        "Jean, Pierre Dupont, Jean Pierre Dupont",
        "Admin, '', Admin",
        "'', Doe, Doe",
    )
    fun `the full name should read as the first name followed by the last name`(firstName: String, lastName: String, expected: String) {
        // Arrange
        val fullName = FullName(FirstName(firstName), LastName(lastName))
        // Act
        val value = fullName.value
        // Assert
        assertThat(value).isEqualTo(expected)
    }

    @ParameterizedTest(name = "\"{0}\" + \"{1}\" -> complete = {2}")
    @CsvSource(
        "Jane, Doe, true",
        "Admin, '', false",
        "'', Doe, false",
        "' ', Doe, false",
    )
    fun `a full name should be complete with a first name and a last name`(firstName: String, lastName: String, complete: Boolean) {
        // Arrange
        val fullName = FullName(FirstName(firstName), LastName(lastName))
        // Act
        val isComplete = fullName.isComplete()
        // Assert
        assertThat(isComplete).isEqualTo(complete)
    }
}
