package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.NominalException
import org.ucop.domain.entity.PreCondition

// The precondition of the use cases reserved to the administrators: the actor, taken from the input of the use
// case, is an existing user with the ADMIN role (read from the users, not from the request)
class IsAdministratorPreCondition<T>(userRepository: UserRepository, actorOf: (T) -> UserId) :
    PreCondition<T>({ input ->
        if (userRepository.isNotAdministrator(actorOf(input))) {
            throw ActorIsNotAdministratorException(ErrorMessage("Only an administrator can do this."))
        }
    })

data class ActorIsNotAdministratorException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
