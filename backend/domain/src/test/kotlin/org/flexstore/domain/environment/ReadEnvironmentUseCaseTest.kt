package org.flexstore.domain.environment

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.usersOfTheAdministration
import org.flexstore.infra.repository.InMemoryEnvironmentRepository
import org.junit.jupiter.api.Test

class ReadEnvironmentUseCaseTest : WithAssertions {

    private val jwtSecret = VariableName("FLEXSTORE_JWT_SECRET")
    private val frontUrl = VariableName("FLEXSTORE_FRONT_URL")
    private val mailPassword = VariableName("FLEXSTORE_MAIL_PASSWORD")
    private val environmentRepository = InMemoryEnvironmentRepository(
        mapOf(
            jwtSecret to VariableValue("a-very-long-secret-of-more-than-32-characters"),
            frontUrl to VariableValue("https://shop.example.com"),
            mailPassword to VariableValue("")
        )
    )
    private val useCase = ReadEnvironmentUseCase(environmentRepository, usersOfTheAdministration())

    @Test
    fun `an administrator should read the state of every variable of the shop`() {
        // Act
        val environmentReport = useCase.unfold(EnvironmentReading(testAdministrator.id))
        // Assert
        val reports = environmentReport.asList().associate { report -> report.variable.name to report.state }
        val expectedVariables = ExpectedVariables.ofTheShop().reportOn { VariableValue.missing() }
        assertThat(reports).hasSameSizeAs(expectedVariables.asList())
        assertThat(reports[jwtSecret]).isEqualTo(VariableState.DefinedAndHidden)
        assertThat(reports[frontUrl])
            .isEqualTo(VariableState.DefinedAndShown(VariableValue("https://shop.example.com")))
        assertThat(reports[mailPassword]).isEqualTo(VariableState.Undefined)
    }

    @Test
    fun `the report should never hold the value of a secret`() {
        // Act
        val environmentReport = useCase.unfold(EnvironmentReading(testAdministrator.id))
        // Assert
        assertThat(environmentReport.toString()).doesNotContain("a-very-long-secret")
    }

    @Test
    fun `the environment should be incomplete while a required variable is undefined`() {
        // Arrange
        val emptyEnvironment = InMemoryEnvironmentRepository()
        val withoutJwtSecret = ReadEnvironmentUseCase(emptyEnvironment, usersOfTheAdministration())
        // Act
        val environmentReport = withoutJwtSecret.unfold(EnvironmentReading(testAdministrator.id))
        // Assert
        assertThat(environmentReport.isComplete()).isFalse()
    }

    @Test
    fun `the environment should be complete once the required variables are defined`() {
        // Act
        val environmentReport = useCase.unfold(EnvironmentReading(testAdministrator.id))
        // Assert
        assertThat(environmentReport.isComplete()).isTrue()
    }

    @Test
    fun `a customer should not read the environment variables`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(EnvironmentReading(testCustomer.id)) }
        // Assert
        assertThat(thrown).isInstanceOf(EnvironmentAdministrationForbiddenException::class.java)
    }
}
