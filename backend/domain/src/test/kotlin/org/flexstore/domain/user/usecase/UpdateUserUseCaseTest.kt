package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.InvalidUserId
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class UpdateUserUseCaseTest {

    private val userRepository = InMemoryUserRepository()
    private val updateUserUseCase = UpdateUserUseCase(userRepository)
    private val administrator = DefinedUser(ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")), Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN)
    private val simpleUser = DefinedUser(ValidUserId("user-1"), FullName(FirstName("Simple"), LastName("User")), Email("simple@example.com"), PlainPassword("user-password"))
    private val john = DefinedUser(ValidUserId("123"), FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))

    init {
        listOf(administrator, simpleUser, john).forEach { userRepository.save(it) }
    }

    private fun stored(userId: UserId) = userRepository.findById(userId) as DefinedUser

    @Test
    fun `an administrator should edit the name, the email and the role of a user`() {
        // Arrange
        val editedJohn = john.copy(fullName = FullName(FirstName("Jane"), LastName("Doe")), email = Email("jane.doe@example.com"), role = Role.ADMIN)
        // Act
        updateUserUseCase.unfold(UpdateUserRequest(administrator.id, editedJohn))
        // Assert
        val storedJohn = stored(john.id)
        assertThat(storedJohn.fullName.value).isEqualTo("Jane Doe")
        assertThat(storedJohn.email).isEqualTo(Email("jane.doe@example.com"))
        assertThat(storedJohn.role).isEqualTo(Role.ADMIN)
    }

    @Test
    fun `an edit should keep the stored password`() {
        // Arrange
        val editedJohn = john.copy(fullName = FullName(FirstName("Jane"), LastName("Doe")), password = PlainPassword("another-password"))
        // Act
        updateUserUseCase.unfold(UpdateUserRequest(administrator.id, editedJohn))
        // Assert
        assertThat(userRepository.passwordMatches(john.email, PlainPassword("password123"))).isTrue()
    }

    @ParameterizedTest(name = "an editor \"{0}\" who is not an administrator is refused")
    @ValueSource(strings = ["user-1", "unknown"])
    fun `should refuse an editor who is not an administrator`(editorId: String) {
        // Arrange
        val editedJohn = john.copy(fullName = FullName(FirstName("Jane"), LastName("Doe")))
        val updateUserRequest = UpdateUserRequest(ValidUserId(editorId), editedJohn)
        // Act
        val thrown = catchThrowable { updateUserUseCase.unfold(updateUserRequest) }
        // Assert
        assertThat(thrown).isInstanceOf(UserUpdateForbiddenException::class.java)
        assertThat(stored(john.id).fullName.value).isEqualTo("John Doe")
    }

    @Test
    fun `should fail to update if user id is invalid`() {
        // Arrange
        val user = john.copy(id = InvalidUserId("@"))
        // Act
        val thrown = catchThrowable { updateUserUseCase.unfold(UpdateUserRequest(administrator.id, user)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidUserIdException::class.java).hasMessage("User ID @ is invalid.")
    }

    @Test
    fun `should fail to update if user does not exist`() {
        // Arrange
        val unknownUser = john.copy(id = ValidUserId("unknown"))
        val updateUserRequest = UpdateUserRequest(administrator.id, unknownUser)
        // Act
        val thrown = catchThrowable { updateUserUseCase.unfold(updateUserRequest) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java).hasMessage("User with ID unknown does not exist.")
    }

    @Test
    fun `should refuse the email of another user`() {
        // Arrange
        val editedJohn = john.copy(email = simpleUser.email)
        // Act
        val thrown = catchThrowable { updateUserUseCase.unfold(UpdateUserRequest(administrator.id, editedJohn)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java)
        assertThat(stored(john.id).email).isEqualTo(Email("john.doe@example.com"))
    }

    @Test
    fun `an edit should keep the status, the phone number and the postal address of a registered customer`() {
        // Arrange
        val registration = RegisterCustomerUseCaseTest.jane
        RegisterCustomerUseCase(userRepository).unfold(registration)
        val editedCustomer = DefinedUser(registration.userId, FullName(FirstName("Janet"), LastName("Doe")), registration.email, PlainPassword(""))
        // Act
        updateUserUseCase.unfold(UpdateUserRequest(administrator.id, editedCustomer))
        // Assert
        val storedCustomer = stored(registration.userId)
        assertThat(storedCustomer.fullName.firstName).isEqualTo(FirstName("Janet"))
        assertThat(storedCustomer.status).isEqualTo(AccountStatus.PENDING_VERIFICATION)
        assertThat(storedCustomer.phoneNumber).isEqualTo(registration.phoneNumber)
        assertThat(storedCustomer.postalAddresses).isEqualTo(PostalAddresses(registration.postalAddress))
    }
}
