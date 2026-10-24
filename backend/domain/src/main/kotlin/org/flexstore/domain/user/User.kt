package org.flexstore.domain.user

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.Actor

sealed class User(open val id: UserId) {
    abstract fun user(userId: UserId):User

    data class DefinedUser(
        override val id: UserId,
        val firstName: FirstName,
        val lastName: LastName,
        val email: Email,
        val password: Password,
        val role: Role = Role.USER
    ) : User(id) {
        override fun user(userId: UserId) = DefinedUser(userId, firstName, lastName, email, password, role)

        fun fullName() = fullNameOf(firstName, lastName)

        fun isAdministrator() = role == Role.ADMIN

        fun isNotAdministrator() = role != Role.ADMIN
    }

    // A user to create: the administrator who creates it chooses its role
    data class DraftUser(
        val firstName: FirstName,
        val lastName: LastName,
        val email: Email,
        val password: Password,
        val role: Role = Role.USER
    ) : User(UserId.InvalidUserId("")) {
        override fun user(userId: UserId) = DraftUser(firstName, lastName, email, password, role)
    }

    data class UndefinedUser(override val id: UserId, val reason: String) : User(id) {
        override fun user(userId: UserId): User = UndefinedUser(userId, reason)
    }

    companion object
}

fun User.Companion.of(firstName: FirstName, lastName: LastName, email: Email, password: Password): User {
    return User.DraftUser(firstName, lastName, email, password)
}

fun User.Companion.of(userId: UserId, firstName: FirstName, lastName: LastName, email: Email, password: Password): User {
    return when (userId.isValid()) {
        true -> User.DefinedUser(userId, firstName, lastName, email, password)
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

data class FirstName(val value: String)

data class LastName(val value: String)

// "First name Last name", as the user is greeted; a missing part is left out
private fun fullNameOf(firstName: FirstName, lastName: LastName) =
    Name(listOf(firstName.value, lastName.value).filter { it.isNotBlank() }.joinToString(" "))

data class Email(val value: String)

// ADMIN can manage the users and the products
enum class Role {
    USER, ADMIN;

    companion object {
        // The role given by name; a user without a given role is a USER
        fun fromName(name: String?): Role =
            if (name.isNullOrBlank()) USER
            else entries.find { it.name == name } ?: throw InvalidRoleException(ErrorMessage("Unknown role: $name"))
    }
}

data class UserUpdateForbiddenException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class UserCreationForbiddenException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvalidRoleException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

sealed class Password {
    abstract val value: String
}

data class PlainPassword(override val value: String) : Password()

data class HashedPassword(override val value: String) : Password()

// creation
data class UserAlreadyExists(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class UserCreationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvalidPasswordException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
// read
data class InvalidUserIdException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class UserNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class UserRetrievalFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
// delete
data class UserDeletionFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
// login
data class InvalidCredentialsException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class UserNotFoundByEmailException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

