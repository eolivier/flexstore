package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor

sealed class User(open val id: UserId) {
    abstract fun user(userId: UserId):User

    data class DefinedUser(
        override val id: UserId,
        val fullName: FullName,
        val email: Email,
        val password: Password,
        val role: Role = Role.USER,
        // A user created by an administrator is active at once
        val status: AccountStatus = AccountStatus.ACTIVE,
        // Given by a customer who creates their own account
        val phoneNumber: PhoneNumber? = null,
        val postalAddresses: PostalAddresses = PostalAddresses.none(),
        // Given by a customer who creates their own account for their company
        val customerType: CustomerType = CustomerType.Individual,
        // Restricted to an ADMIN account in this iteration
        val twoFactorAuthentication: TwoFactorAuthentication = TwoFactorAuthentication.Disabled
    ) : User(id) {
        override fun user(userId: UserId) = copy(id = userId)

        // The addresses where a customer has an order delivered or billed become addresses of the customer
        fun withPostalAddressesIncluding(vararg addresses: PostalAddress) =
            copy(postalAddresses = postalAddresses.including(*addresses))

        fun withPostalAddresses(postalAddresses: PostalAddresses) = copy(postalAddresses = postalAddresses)

        fun account() = Account(
            id, fullName, email, role, status, phoneNumber, postalAddresses,
            customerType, twoFactorAuthentication
        )

        // Too many failed sign-ins: an administrator reactivates the account
        fun locked() = copy(status = AccountStatus.LOCKED)

        fun isAdministrator() = role == Role.ADMIN

        fun isSystem() = role.isSystem()

        fun isNotAdministrator() = role != Role.ADMIN

        // A new enrollment is generated, kept pending until the first code confirms it
        fun withTwoFactorAuthenticationPending(secret: TwoFactorSecret) =
            copy(twoFactorAuthentication = TwoFactorAuthentication.PendingConfirmation(secret))

        fun withTwoFactorAuthenticationEnabled(secret: TwoFactorSecret) =
            copy(twoFactorAuthentication = TwoFactorAuthentication.Enabled(secret))

        fun withTwoFactorAuthenticationDisabled() =
            copy(twoFactorAuthentication = TwoFactorAuthentication.Disabled)
    }

    // A user to create: the administrator who creates it chooses its role
    data class DraftUser(
        val fullName: FullName,
        val email: Email,
        val password: Password,
        val role: Role = Role.USER
    ) : User(UserId.InvalidUserId("")) {
        override fun user(userId: UserId) = DraftUser(fullName, email, password, role)
    }

    data class UndefinedUser(override val id: UserId, val reason: String) : User(id) {
        override fun user(userId: UserId): User = UndefinedUser(userId, reason)
    }

    companion object
}

fun User.Companion.of(fullName: FullName, email: Email, password: Password): User {
    return User.DraftUser(fullName, email, password)
}

fun User.Companion.of(userId: UserId, fullName: FullName, email: Email, password: Password): User {
    return when (userId.isValid()) {
        true -> User.DefinedUser(userId, fullName, email, password)
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

// The system user, created by the migration V62: it runs the scheduled use cases
object SystemUser {
    val id = UserId.ValidUserId("system")
}

fun UserId.Companion.isValid(value: String) = value.isNotEmpty()

fun UserId.Companion.nextId(): UserId = UserId.ValidUserId(Identity.nextId().value)

fun UserId.Companion.of(value: String): UserId {
    return when (UserId.isValid(value)) {
        true -> UserId.ValidUserId(value)
        false -> UserId.InvalidUserId(value)
    }
}

data class Email(val value: String)

// ADMIN can manage the users and the products. SYSTEM is the user that runs the scheduled use cases: nobody
// signs in with it, nobody gives it to a user
enum class Role {
    USER, ADMIN, SYSTEM;

    fun isSystem() = this == SYSTEM

    fun isNotSystem() = !isSystem()

    companion object {
        // The role given by name; a user without a given role is a USER
        fun fromName(name: String?): Role =
            if (name.isNullOrBlank()) USER
            else entries.find { it.name == name } ?: throw InvalidRoleException(ErrorMessage("Unknown role: $name"))
    }
}

data class UserReadingForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class UserDeletionForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class UserUpdateForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class UserCreationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class SystemSignInForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class SystemRoleForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidRoleException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)

sealed class Password {
    abstract val value: String
}

data class PlainPassword(override val value: String) : Password()

data class HashedPassword(override val value: String) : Password()

// creation
data class UserAlreadyExists(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class UserCreationFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class InvalidPasswordException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
// read
data class InvalidUserIdException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class UserNotFoundException(override val errorMessage: ErrorMessage) : PreConditionException(errorMessage)
data class UserRetrievalException(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
// delete
data class UserDeletionFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
// login
data class InvalidCredentialsException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
// The user was not authenticated although the login preconditions held
data class UserSignInException(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
data class UserGoogleAuthenticationException(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
data class UserNotFoundByEmailException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)

