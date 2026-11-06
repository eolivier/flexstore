package org.flexstore.domain.environment

import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException

data class VariableName(val value: String)

// What the environment holds for a variable: a variable that is not set, or null, is an empty value
data class VariableValue(val value: String) {

    // Valid: neither null, nor empty, nor blank
    fun isFilled() = value.isNotBlank()

    companion object {
        fun missing() = VariableValue("")
    }
}

// A password, a key or a secret must never be read: only its definition is shown
enum class Sensitivity { PLAIN, SECRET }

// A required variable is needed by the shop to work properly, an optional one has a default or turns a
// feature on
enum class Necessity { REQUIRED, OPTIONAL }

data class VariableProfile(val sensitivity: Sensitivity, val necessity: Necessity) {

    fun isSecret() = sensitivity == Sensitivity.SECRET

    fun isRequired() = necessity == Necessity.REQUIRED
}

// What an administrator learns about a variable: that it is undefined, or that it is defined with its value
// shown (a plain variable) or hidden (a secret)
sealed interface VariableState {

    fun isDefined(): Boolean

    fun isUndefined() = !isDefined()

    data object Undefined : VariableState {
        override fun isDefined() = false
    }

    data class DefinedAndShown(val value: VariableValue) : VariableState {
        override fun isDefined() = true
    }

    data object DefinedAndHidden : VariableState {
        override fun isDefined() = true
    }
}

data class ExpectedVariable(val name: VariableName, val profile: VariableProfile) {

    fun reportOn(value: VariableValue) = VariableReport(this, stateOf(value))

    // A secret never leaves this method: it only tells whether it is defined
    private fun stateOf(value: VariableValue): VariableState = when {
        value.isFilled() && profile.isSecret() -> VariableState.DefinedAndHidden
        value.isFilled() -> VariableState.DefinedAndShown(value)
        else -> VariableState.Undefined
    }

    companion object {
        fun requiredPlain(name: VariableName) =
            ExpectedVariable(name, VariableProfile(Sensitivity.PLAIN, Necessity.REQUIRED))

        fun requiredSecret(name: VariableName) =
            ExpectedVariable(name, VariableProfile(Sensitivity.SECRET, Necessity.REQUIRED))

        fun optionalPlain(name: VariableName) =
            ExpectedVariable(name, VariableProfile(Sensitivity.PLAIN, Necessity.OPTIONAL))

        fun optionalSecret(name: VariableName) =
            ExpectedVariable(name, VariableProfile(Sensitivity.SECRET, Necessity.OPTIONAL))
    }
}

data class VariableReport(val variable: ExpectedVariable, val state: VariableState) {

    fun isRequiredButUndefined() = variable.profile.isRequired() && state.isUndefined()
}

data class EnvironmentReport(private val variableReports: List<VariableReport>) {

    fun isComplete() = variableReports.none { it.isRequiredButUndefined() }

    fun asList(): List<VariableReport> = variableReports.toList()
}

// The variables the shop reads, as described in the README. The database is given either by its URL or by the
// separate variables, so none of them is required alone
data class ExpectedVariables(private val variables: List<ExpectedVariable>) {

    // Reads each variable through the environment given (the values stay in the infrastructure until reported)
    fun reportOn(environment: (VariableName) -> VariableValue) =
        EnvironmentReport(variables.map { variable -> variable.reportOn(environment(variable.name)) })

    companion object {
        fun ofTheShop() = ExpectedVariables(
            listOf(
                ExpectedVariable.requiredSecret(VariableName("FLEXSTORE_JWT_SECRET")),
                ExpectedVariable.requiredPlain(VariableName("FLEXSTORE_FRONT_URL")),
                ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_DATABASE_URL")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_DATASOURCE_HOST")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_DATASOURCE_HOST_SUFFIX")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_DATASOURCE_PORT")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_DATASOURCE_DATABASE")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_DATASOURCE_USERNAME")),
                ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_DATASOURCE_PASSWORD")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_HOST")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_PORT")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_USERNAME")),
                ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_MAIL_PASSWORD")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_SMTP_AUTH")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_SMTP_STARTTLS")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MAIL_FROM")),
                ExpectedVariable.optionalPlain(VariableName("FLEXSTORE_MCP_ENABLED")),
                ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_STRIPE_SECRET_KEY")),
                ExpectedVariable.optionalSecret(VariableName("FLEXSTORE_STRIPE_WEBHOOK_SECRET"))
            )
        )
    }
}

// An administrator who reads the state of the environment variables
data class EnvironmentReading(val reader: UserId)

data class EnvironmentAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
