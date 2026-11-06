package org.flexstore.domain.environment

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ExpectedVariableTest : WithAssertions {

    private val plainVariable = ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_HOST"))
    private val secretVariable = ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_MAIL_PASSWORD"))

    @Test
    fun `a plain variable with a value should be defined and show its value`() {
        // Act
        val report = plainVariable.reportOn(VariableValue("smtp.example.com"))
        // Assert
        assertThat(report.state).isEqualTo(VariableState.DefinedAndShown(VariableValue("smtp.example.com")))
    }

    @Test
    fun `a secret with a value should be defined and hide its value`() {
        // Act
        val report = secretVariable.reportOn(VariableValue("s3cret"))
        // Assert
        assertThat(report.state).isEqualTo(VariableState.DefinedAndHidden)
        assertThat(report.toString()).doesNotContain("s3cret")
    }

    @ParameterizedTest(name = "the value ''{0}'' is not a valid value")
    @ValueSource(strings = ["", " ", "   "])
    fun `a variable with an empty or blank value should be undefined`(value: String) {
        // Act
        val report = secretVariable.reportOn(VariableValue(value))
        // Assert
        assertThat(report.state).isEqualTo(VariableState.Undefined)
    }

    @Test
    fun `a variable that is not set should be undefined`() {
        // Act
        val report = plainVariable.reportOn(VariableValue.missing())
        // Assert
        assertThat(report.state).isEqualTo(VariableState.Undefined)
    }

    @Test
    fun `a required variable that is undefined should be reported as missing`() {
        // Arrange
        val requiredVariable = ExpectedVariable.requiredSecret(VariableName("FLEXSTORE_JWT_SECRET"))
        // Act
        val report = requiredVariable.reportOn(VariableValue.missing())
        // Assert
        assertThat(report.isRequiredButUndefined()).isTrue()
    }

    @Test
    fun `an optional variable that is undefined should not be reported as missing`() {
        // Act
        val report = plainVariable.reportOn(VariableValue.missing())
        // Assert
        assertThat(report.isRequiredButUndefined()).isFalse()
    }
}
