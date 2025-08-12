package org.flexstore.domain.entity

import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.Actor
import org.ucop.domain.entity.Name

sealed class User(open val id: UserId) {
    abstract fun user(userId: UserId):User

    data class DefinedUser(override val id: UserId, val name: Name, val email: Email) : User(id) {
        override fun user(userId: UserId) = DefinedUser(userId, name, email)
    }

    data class UndefinedUser(override val id: UserId, val reason: String) : User(id) {
        override fun user(userId: UserId): User = UndefinedUser(userId, reason)
    }

    companion object
}

fun User.Companion.of(userId: UserId, name: Name, email: Email): User {
    return when (userId.isValid()) {
        true -> User.DefinedUser(userId, name, email)
        false -> User.UndefinedUser(userId, "Invalid user id")
    }
}

sealed class UserId(open val value: String) {

    abstract fun isValid(): Boolean
    fun isInvalid() = !isValid()

    data class ValidUserId(override val value: String) : UserId(value){
        override fun isValid() = UserId.isValid(value)
    }
    data class InvalidUserId(override val value: String) : UserId(value) {
        override fun isValid() = false
    }

    companion object
}

fun UserId.Companion.isValid(value: String) = value.isNotEmpty()

fun UserId.Companion.of(value: String): UserId {
    return when (UserId.isValid(value)) {
        true -> UserId.ValidUserId(value)
        false -> UserId.InvalidUserId(value)
    }
}

data class Email(val value: String)

class Guest(name: Name) : Actor(name)

// creation
data class UserAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// read
data class InvalidUserIdException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserRetrievalFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// delete
data class UserDeletionFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)

