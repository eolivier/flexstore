package org.flexstore.domain.actor

import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.Actor

// Finds who is behind a user id before a use case runs, so that the use case receives its actor as an argument:
// an unknown user is a Visitor, a user with the USER role a Customer, a user with the ADMIN role an Admin
class ActorResolutionService(private val userRepository: UserRepository) {

    fun actorOf(userId: UserId): Actor {
        if (userRepository.notExists(userId)) return Visitor()
        val user = userRepository.findById(userId)
        if (user !is User.DefinedUser) return Visitor()
        val name = Name(user.fullName.value)
        return if (user.isAdministrator()) Admin(userId, name) else Customer(userId, name)
    }
}
