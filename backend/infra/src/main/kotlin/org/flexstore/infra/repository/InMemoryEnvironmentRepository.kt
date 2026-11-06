package org.flexstore.infra.repository

import org.flexstore.domain.environment.*

class InMemoryEnvironmentRepository(private val values: Map<VariableName, VariableValue> = emptyMap()) :
    EnvironmentRepository {

    override fun valueOf(name: VariableName): VariableValue = values[name] ?: VariableValue.missing()
}
