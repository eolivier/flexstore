package org.flexstore.domain.entity

import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.Actor
import org.ucop.domain.entity.Name

sealed class User(open val id: UserId) {
    data class DefinedUser(override val id: UserId, val name: Name, val email: Email) : User(id)
    data class UndefinedUser(override val id: UserId, val reason: String) : User(id)
}
sealed class UserId(open val value: String) {
    abstract fun isValid(): Boolean
    data class ValidUserId(override val value: String) : UserId(value) {
        override fun isValid() = value.isNotEmpty()
    }
    data class InvalidUserId(override val value: String) : UserId(value) {
        override fun isValid() = false
    }
}
data class Email(val value: String)

class Guest(name: Name) : Actor(name)

// creation
data class UserAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// read
data class UserNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserRetrievalFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// delete
data class UserDeletionFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)

