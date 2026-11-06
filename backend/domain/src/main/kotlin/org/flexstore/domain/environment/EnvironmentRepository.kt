package org.flexstore.domain.environment

// Port: the environment variables of the running back end
interface EnvironmentRepository {

    // The value of a variable, missing() when it is not set
    fun valueOf(name: VariableName): VariableValue
}
