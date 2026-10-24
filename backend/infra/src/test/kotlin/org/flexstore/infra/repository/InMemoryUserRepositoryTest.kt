package org.flexstore.infra.repository

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
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
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
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
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
        // Act
        userRepository.save(user)
        // Assert
        assertThat(userRepository.exists(user.id)).isTrue()
    }

    @Test
    fun `should find user by id`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)
        // Act
        val foundUser = userRepository.findById(user.id)
        // Assert
        assertThat(foundUser.id).isEqualTo(user.id)
        assertThat((foundUser as DefinedUser).name).isEqualTo(user.name)
        assertThat(foundUser.email).isEqualTo(user.email)
        // Password will be hashed, so we can't directly compare
        assertThat(foundUser.password).isInstanceOf(HashedPassword::class.java)
    }

    @Test
    fun `should return true when user is deleted successfully`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
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
        val draftUser = User.DraftUser(Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"), role)
        // Act
        val savedUser = userRepository.save(draftUser)
        // Assert
        assertThat((savedUser as DefinedUser).role).isEqualTo(role)
    }
}
