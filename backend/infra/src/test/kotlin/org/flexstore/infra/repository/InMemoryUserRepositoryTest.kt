package org.flexstore.infra.repository

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class InMemoryUserRepositoryTest {

    @Test
    fun `should return false when user does not exist`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val userId = ValidUserId("123")
        // Act
        val exists = userRepository.exists(userId)
        // Assert
        assertThat(exists).isFalse()
    }

    @Test
    fun `should return true when user exists`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)
        // Act
        val exists = userRepository.exists(user.id)
        // Assert
        assertThat(exists).isTrue()
    }

    @Test
    fun `should save user successfully`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        // Act
        userRepository.save(user)
        // Assert
        assertThat(userRepository.exists(user.id)).isTrue()
    }

    @Test
    fun `should find user by id`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)
        // Act
        val foundUser = userRepository.findById(user.id)
        // Assert
        assertThat(foundUser.id).isEqualTo(user.id)
        assertThat((foundUser as DefinedUser).fullName).isEqualTo(user.fullName)
        assertThat(foundUser.email).isEqualTo(user.email)
        // Password will be hashed, so we can't directly compare
        assertThat(foundUser.password).isInstanceOf(HashedPassword::class.java)
    }

    @Test
    fun `should return true when user is deleted successfully`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)
        // Act
        val deleted = userRepository.delete(user.id)
        // Assert
        assertThat(deleted).isTrue()
        assertThat(userRepository.exists(user.id)).isFalse()
    }

    @Test
    fun `should return false when user deletion fails`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val userId = ValidUserId("123")
        // Act
        val deleted = userRepository.delete(userId)
        // Assert
        assertThat(deleted).isFalse()
    }

    @ParameterizedTest(name = "a draft {0} is saved as a {0}")
    @EnumSource(Role::class)
    fun `should keep the role of a created user`(role: Role) {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val draftUser = User.DraftUser(FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"), role)
        // Act
        val savedUser = userRepository.save(draftUser)
        // Assert
        assertThat((savedUser as DefinedUser).role).isEqualTo(role)
    }

    @ParameterizedTest(name = "user \"{0}\" is not an administrator: {1}")
    @CsvSource("admin-1, false", "user-1, true", "unknown, true")
    fun `only an existing ADMIN is an administrator`(userId: String, notAdministrator: Boolean) {
        // Arrange
        val userRepository = InMemoryUserRepository()
        userRepository.save(DefinedUser(ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")), Email("admin@example.com"), PlainPassword("password123"), Role.ADMIN))
        userRepository.save(DefinedUser(ValidUserId("user-1"), FullName(FirstName("Simple"), LastName("User")), Email("user@example.com"), PlainPassword("password123")))
        // Act
        val isNotAdministrator = userRepository.isNotAdministrator(ValidUserId(userId))
        // Assert
        assertThat(isNotAdministrator).isEqualTo(notAdministrator)
    }

    @ParameterizedTest(name = "the email of user \"{0}\" checked for user \"{1}\": used by another one = {2}")
    @CsvSource("user-1, user-1, false", "user-1, user-2, true")
    fun `an email is used by another user when it belongs to someone else`(owner: String, checkedFor: String, usedByAnother: Boolean) {
        // Arrange
        val userRepository = InMemoryUserRepository()
        userRepository.save(DefinedUser(ValidUserId(owner), FullName(FirstName("Simple"), LastName("User")), Email("user@example.com"), PlainPassword("password123")))
        // Act
        val isUsedByAnother = userRepository.emailIsUsedByAnotherUser(Email("user@example.com"), ValidUserId(checkedFor))
        // Assert
        assertThat(isUsedByAnother).isEqualTo(usedByAnother)
    }
}
