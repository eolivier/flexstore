package org.flexstore.domain.user.model

import org.flexstore.domain.shared.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.Actor

sealed class User(open val id: UserId) {
    abstract fun user(userId: UserId):User

    data class DefinedUser(
        override val id: UserId,
        val name: Name,
        val email: Email,
        val password: Password,
        val role: Role = Role.USER
    ) : User(id) {
        override fun user(userId: UserId) = DefinedUser(userId, name, email, password, role)
    }

    data class DraftUser(val name: Name, val email: Email, val password: Password) : User(UserId.InvalidUserId("")) {
        override fun user(userId: UserId) = DraftUser(name, email, password)
    }

    data class UndefinedUser(override val id: UserId, val reason: String) : User(id) {
        override fun user(userId: UserId): User = UndefinedUser(userId, reason)
    }

    companion object
}

fun User.Companion.of(name: Name, email: Email, password: Password): User {
    return User.DraftUser(name, email, password)
}

fun User.Companion.of(userId: UserId, name: Name, email: Email, password: Password): User {
    return when (userId.isValid()) {
        true -> User.DefinedUser(userId, name, email, password)
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

// ADMIN can manage the users and the products
enum class Role { USER, ADMIN }

sealed class Password {
    abstract val value: String
}

data class PlainPassword(override val value: String) : Password()

data class HashedPassword(override val value: String) : Password()

// creation
data class UserAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class InvalidPasswordException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// read
data class InvalidUserIdException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserRetrievalFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// delete
data class UserDeletionFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
// login
data class InvalidCredentialsException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class UserNotFoundByEmailException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)

