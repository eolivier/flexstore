package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator who reads a page of the users, all of them or the ones that match a search
data class UsersReading(val reader: Actor, val usersSearch: UsersSearch) {

    constructor(reader: Actor, pageRequest: PageRequest) : this(reader, UsersSearch(pageRequest))
}

// An administrator reads a page of the users of the shop, customers and administrators, searched or not
class ListUsersUseCase(private val userRepository: UserRepository) : QueryUseCase<UsersReading, Page<Users>> {

    override fun getPreConditions(): PreConditions<UsersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<UsersReading, Page<Users>> { reading ->
        userRepository.findAll(reading.usersSearch)
    }

    override fun getPostConditions(): PostConditions<Page<Users>> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<UsersReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            throw UserReadingForbiddenException(ErrorMessage("Only an administrator can read the users."))
        }
    }
}
