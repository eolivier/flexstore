package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// An administrator who reads a page of the users
data class UsersReading(val reader: UserId, val pageRequest: PageRequest)

// An administrator reads a page of the users of the shop, customers and administrators
class ListUsersUseCase(private val userRepository: UserRepository) : QueryUseCase<UsersReading, Page<Users>> {

    override fun getPreConditions(): PreConditions<UsersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<UsersReading, Page<Users>> { reading ->
        userRepository.findAll(reading.pageRequest)
    }

    override fun getPostConditions(): PostConditions<Page<Users>> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<UsersReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw UserReadingForbiddenException(ErrorMessage("Only an administrator can read the users."))
        }
    }
}
