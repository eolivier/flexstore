package org.flexstore.domain.environment

import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator checks which environment variables of the shop are defined: the plain ones with their value,
// the secrets (passwords, keys) only as defined or not
class ReadEnvironmentUseCase(
    private val environmentRepository: EnvironmentRepository
) : QueryUseCase<EnvironmentReading, EnvironmentReport> {

    override fun getPreConditions(): PreConditions<EnvironmentReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<EnvironmentReading, EnvironmentReport> {
        ExpectedVariables.ofTheShop().reportOn { name -> environmentRepository.valueOf(name) }
    }

    override fun getPostConditions(): PostConditions<EnvironmentReport> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<EnvironmentReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can read the environment variables.")
            throw EnvironmentAdministrationForbiddenException(message)
        }
    }
}
