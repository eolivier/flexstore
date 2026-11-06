package org.flexstore.infra.spring.adapter.environment

import org.flexstore.domain.environment.EnvironmentRepository
import org.flexstore.domain.environment.VariableName
import org.flexstore.domain.environment.VariableValue
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

// The environment variables of the back end, read through the Spring environment (system variables first)
@Component
class SpringEnvironmentRepository(private val environment: Environment) : EnvironmentRepository {

    override fun valueOf(name: VariableName): VariableValue {
        val variable = environment.getProperty(name.value) ?: return VariableValue.missing()
        return VariableValue(variable)
    }
}
