package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.environment.*

// sensitivity: PLAIN or SECRET; necessity: REQUIRED or OPTIONAL; status: DEFINED or UNDEFINED (a defined
// variable is neither empty, nor blank); value: only the value of a defined plain variable, never the one of a
// secret
@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonEnvironmentVariable(
    @JsonProperty("name") val name: String,
    @JsonProperty("sensitivity") val sensitivity: String,
    @JsonProperty("necessity") val necessity: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("value") val value: String?
) {
    constructor(report: VariableReport) : this(
        report.variable.name.value,
        report.variable.profile.sensitivity.name,
        report.variable.profile.necessity.name,
        if (report.state.isDefined()) STATUS_DEFINED else STATUS_UNDEFINED,
        shownValueOf(report.state)
    )

    companion object {
        private const val STATUS_DEFINED = "DEFINED"
        private const val STATUS_UNDEFINED = "UNDEFINED"

        private fun shownValueOf(state: VariableState): String? = when (state) {
            is VariableState.DefinedAndShown -> state.value.value
            VariableState.DefinedAndHidden, VariableState.Undefined -> null
        }

        fun listOf(environmentReport: EnvironmentReport) =
            environmentReport.asList().map { report -> JsonEnvironmentVariable(report) }
    }
}
