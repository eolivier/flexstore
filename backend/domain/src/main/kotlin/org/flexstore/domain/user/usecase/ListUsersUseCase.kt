package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// An administrator who reads all the users
data class UsersReading(val reader: UserId)

// An administrator reads all the users of the shop, customers and administrators
class ListUsersUseCase(private val userRepository: UserRepository) : QueryUseCase<UsersReading, Users> {

    override fun getPreConditions(): PreConditions<UsersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<UsersReading, Users> { Users(userRepository.findAll()) }

    override fun getPostConditions(): PostConditions<Users> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<UsersReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw UserReadingForbiddenException(ErrorMessage("Only an administrator can read the users."))
        }
    }
}
