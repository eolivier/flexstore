package org.flexstore.infra.repository

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
}