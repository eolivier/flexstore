package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.Name
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserFullNameTest : WithAssertions {

    @ParameterizedTest(name = "\"{0}\" + \"{1}\" -> \"{2}\"")
    @CsvSource(
        "Jane, Doe, Jane Doe",
        "Jean, Pierre Dupont, Jean Pierre Dupont",
        "Admin, '', Admin",
    )
    fun `the full name should be the first name followed by the last name`(firstName: String, lastName: String, expected: String) {
        // Arrange
        val user = User.DefinedUser(UserId.ValidUserId("u-1"), FirstName(firstName), LastName(lastName), Email("jane@example.com"), PlainPassword("password"))
        // Act
        val fullName = user.fullName()
        // Assert
        assertThat(fullName).isEqualTo(Name(expected))
    }
}
