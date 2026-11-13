package org.flexstore.domain.actor

import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.Actor

// Finds who is behind a user id before a use case runs, so that the use case receives its actor as an argument:
// an unknown user is a Visitor, a user with the USER role a Customer, a user with the ADMIN role an Admin, the
// user with the SYSTEM role the SystemActor;
// a Customer or an Admin knows the status of its account, so whether it can act
class ActorResolutionService(private val userRepository: UserRepository) {

    // The system user, who runs the scheduled use cases
    fun system(): Actor = actorOf(SystemUser.id)

    // Who is not signed in: a Visitor
    fun visitor(): Actor = Visitor()

    fun actorOf(userId: UserId): Actor {
        if (userRepository.notExists(userId)) return Visitor()
        val user = userRepository.findById(userId)
        return if (user is User.DefinedUser) actorOf(user) else Visitor()
    }

    private fun actorOf(user: User.DefinedUser): Actor {
        if (user.isSystem()) return SystemActor()
        val name = Name(user.fullName.value)
        val validUserId = UserId.ValidUserId(user.id.value)
        return if (user.isAdministrator()) Admin(validUserId, name, user.status)
        else Customer(validUserId, name, user.status)
    }
}
