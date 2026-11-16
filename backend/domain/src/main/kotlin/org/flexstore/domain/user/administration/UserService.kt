package org.flexstore.domain.user.administration

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.usecase.ChangePasswordRequest
import org.flexstore.domain.user.administration.usecase.AccountStatusChange
import org.flexstore.domain.user.administration.usecase.CreateUserRequest
import org.flexstore.domain.user.administration.usecase.UpdateUserRequest
import org.flexstore.domain.user.administration.usecase.UserDeletion
import org.flexstore.domain.user.administration.usecase.UserReading
import org.flexstore.domain.user.administration.usecase.UsersReading

class UserService(
    private val userRepository: UserRepository,
    private val userAdministrationUseCases: UserAdministrationUseCases
) {

    // A draft user only gets its ID from the repository: the created user is read back by its email
    fun createUser(request: CreateUserRequest): User {
        userAdministrationUseCases.createUserUseCase.unfold(request)
        return when (val user = request.user) {
            is User.DraftUser -> userRepository.findByEmail(user.email)
            else -> userRepository.findById(user.id)
        }
    }

    fun readUser(reader: Actor, userId: UserId): User =
        userAdministrationUseCases.readUserUseCase.unfold(UserReading(reader, userId))

    fun updateUser(request: UpdateUserRequest) {
        userAdministrationUseCases.updateUserUseCase.unfold(request)
    }

    fun changeAccountStatus(change: AccountStatusChange): User {
        userAdministrationUseCases.changeAccountStatusUseCase.unfold(change)
        return readUser(change.editor, change.userId)
    }

    fun deleteUser(editor: Actor, userId: UserId) {
        userAdministrationUseCases.deleteUserUseCase.unfold(UserDeletion(editor, userId))
    }

    fun readAllUsers(reader: Actor, usersSearch: UsersSearch): Page<Users> =
        userAdministrationUseCases.listUsersUseCase.unfold(UsersReading(reader, usersSearch))

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        userAdministrationUseCases.changePasswordUseCase.unfold(changePasswordRequest)
    }
}
