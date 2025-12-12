package org.flexstore.infra.repository

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.valueobject.Name
import org.junit.jupiter.api.Test

class InMemoryUserRepositoryTest {

    @Test
    fun `should return false when user does not exist`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val userId = ValidUserId("123")

        // Act
        val result = userRepository.exists(userId)

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when user exists`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)

        // Act
        val result = userRepository.exists(user.id)

        // Assert
        assertThat(result).isTrue()
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
        val result = userRepository.findById(user.id)

        // Assert
        assertThat(result.id).isEqualTo(user.id)
        assertThat((result as DefinedUser).name).isEqualTo(user.name)
        assertThat(result.email).isEqualTo(user.email)
        // Password will be hashed, so we can't directly compare
        assertThat(result.password).isInstanceOf(HashedPassword::class.java)
    }

    @Test
    fun `should return true when user is deleted successfully`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)

        // Act
        val result = userRepository.delete(user.id)

        // Assert
        assertThat(result).isTrue()
        assertThat(userRepository.exists(user.id)).isFalse()
    }

    @Test
    fun `should return false when user deletion fails`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val userId = ValidUserId("123")

        // Act
        val result = userRepository.delete(userId)

        // Assert
        assertThat(result).isFalse()
    }
}