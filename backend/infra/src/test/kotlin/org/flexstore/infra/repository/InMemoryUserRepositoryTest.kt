package org.flexstore.infra.repository

import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.valueobject.Name
import org.junit.jupiter.api.Assertions.*
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
        assertFalse(result)
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
        assertTrue(result)
    }

    @Test
    fun `should save user successfully`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))

        // Act
        userRepository.save(user)

        // Assert
        assertTrue(userRepository.exists(user.id))
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
        assertEquals(user.id, result.id)
        assertEquals(user.name, (result as DefinedUser).name)
        assertEquals(user.email, result.email)
        // Password will be hashed, so we can't directly compare
        assertTrue(result.password is HashedPassword)
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
        assertTrue(result)
        assertFalse(userRepository.exists(user.id))
    }

    @Test
    fun `should return false when user deletion fails`() {
        // Arrange
        val userRepository = InMemoryUserRepository()
        val userId = ValidUserId("123")

        // Act
        val result = userRepository.delete(userId)

        // Assert
        assertFalse(result)
    }
}