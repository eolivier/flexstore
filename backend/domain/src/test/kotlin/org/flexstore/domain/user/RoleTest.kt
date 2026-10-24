package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RoleTest : WithAssertions {

    @ParameterizedTest(name = "\"{0}\" is the role {1}")
    @CsvSource("ADMIN, ADMIN", "USER, USER", ", USER", "' ', USER")
    fun `a role is found by its name, USER by default`(name: String?, expectedRole: Role) {
        // Act
        val role = Role.fromName(name)
        // Assert
        assertThat(role).isEqualTo(expectedRole)
    }

    @Test
    fun `an unknown role should be rejected`() {
        // Act
        val thrown = catchThrowable { Role.fromName("SUPERVISOR") }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidRoleException::class.java)
    }
}
