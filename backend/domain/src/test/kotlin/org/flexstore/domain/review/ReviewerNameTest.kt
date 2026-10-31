package org.flexstore.domain.review

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReviewerNameTest : WithAssertions {

    @ParameterizedTest(name = "{0} {1} is named ''{2}''")
    @CsvSource("Marie, Durand, Marie D.", "marie, durand, marie D.", "Marie, '', Marie", "'', Durand, A customer")
    fun `a reviewer should be named by their first name and the initial of their last name`(
        firstName: String, lastName: String, expected: String
    ) {
        // Act
        val name = ReviewerName.of(FullName(FirstName(firstName), LastName(lastName)))
        // Assert
        assertThat(name).isEqualTo(ReviewerName(expected))
    }
}
