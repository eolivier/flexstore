package org.flexstore.domain.environment

import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator checks which environment variables of the shop are defined: the plain ones with their value,
// the secrets (passwords, keys) only as defined or not
class ReadEnvironmentUseCase(
    private val environmentRepository: EnvironmentRepository,
    private val userRepository: UserRepository
) : QueryUseCase<EnvironmentReading, EnvironmentReport> {

    override fun getPreConditions(): PreConditions<EnvironmentReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<EnvironmentReading, EnvironmentReport> {
        ExpectedVariables.ofTheShop().reportOn { name -> environmentRepository.valueOf(name) }
    }

    override fun getPostConditions(): PostConditions<EnvironmentReport> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<EnvironmentReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw EnvironmentAdministrationForbiddenException(
                ErrorMessage("Only an administrator can read the environment variables.")
            )
        }
    }
}
