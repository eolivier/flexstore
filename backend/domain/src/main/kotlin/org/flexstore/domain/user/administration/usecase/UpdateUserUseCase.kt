package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A user is edited by an administrator (the editor): name, email and role. The password is changed by a separate
// operation
data class UpdateUserRequest(val editor: UserId, val user: User)

class UpdateUserUseCase(private val userRepository: UserRepository) : UseCase<UpdateUserRequest> {

    override fun getPreConditions(): PreConditions<UpdateUserRequest> = PreConditions(
        editorIsAdministratorCondition(),
        isValidUserIdCondition(),
        userDoExistCondition(),
        roleIsNotSystemCondition(),
        emailIsNotUsedByAnotherUserCondition()
    )

    override fun getNominalScenario(): NominalScenario<UpdateUserRequest> {
        val updateUserStep = Step<UpdateUserRequest> { request ->
            userRepository.save(withStoredAccount(request.user))
        }
        return NominalScenario(listOf(updateUserStep))
    }

    override fun getPostConditions(): PostConditions<UpdateUserRequest> = PostConditions.none()

    // An edit changes neither the password (the stored hashed one is kept) nor the status, the contact details,
    // the company given by the customer, nor the two-factor authentication (removed by its own operation)
    private fun withStoredAccount(user: User): User {
        if (user !is User.DefinedUser) {
            return user
        }
        return when (val existingUser = userRepository.findById(user.id)) {
            is User.DefinedUser -> user.copy(
                password = existingUser.password,
                status = existingUser.status,
                phoneNumber = existingUser.phoneNumber,
                postalAddresses = existingUser.postalAddresses,
                customerType = existingUser.customerType,
                twoFactorAuthentication = existingUser.twoFactorAuthentication
            )
            else -> user
        }
    }

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<UpdateUserRequest> { request ->
        if (userRepository.isNotAdministrator(request.editor)) {
            throw UserUpdateForbiddenException(ErrorMessage("Only an administrator can edit a user."))
        }
    }

    // The system user cannot be edited, and no other user becomes the system
    private fun roleIsNotSystemCondition() = PreCondition<UpdateUserRequest> { request ->
        val user = request.user
        val storedUser = userRepository.findById(user.id)
        val editsTheSystem = storedUser is User.DefinedUser && storedUser.isSystem()
        val givesTheSystemRole = user is User.DefinedUser && user.isSystem()
        if (editsTheSystem || givesTheSystemRole) {
            throw SystemRoleForbiddenException(ErrorMessage("The system user cannot be edited nor given."))
        }
    }

    private fun isValidUserIdCondition() = PreCondition<UpdateUserRequest> { request ->
        if (request.user.id.isInvalid()) {
            throw InvalidUserIdException(ErrorMessage("User ID ${request.user.id.value} is invalid."))
        }
    }

    private fun userDoExistCondition() = PreCondition<UpdateUserRequest> { request ->
        if (userRepository.notExists(request.user.id)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${request.user.id.value} does not exist."))
        }
    }

    private fun emailIsNotUsedByAnotherUserCondition() = PreCondition<UpdateUserRequest> { request ->
        val user = request.user
        if (user is User.DefinedUser && userRepository.emailIsUsedByAnotherUser(user.email, user.id)) {
            throw UserAlreadyExists(ErrorMessage("User with email ${user.email.value} already exists."))
        }
    }
}
