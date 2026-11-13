package org.flexstore.infra.spring.adapter.persistence.jpa

import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.PostgresUserRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.Optional
import java.util.function.Supplier

// The cache of the reads of the user table, with the real adapter behind the Spring cache, without a database
class UserReadCacheTest {

    private val janeId = UserId.ValidUserId("user-1")
    private val bobId = UserId.ValidUserId("user-2")
    private val janeEntity = UserEntity(janeId.value, "Jane", "Doe", "jane.doe@example.com", "hashed")
    private val bobEntity = UserEntity(bobId.value, "Bob", "Martin", "bob.martin@example.com", "hashed")
    private val updatedJane = User.DefinedUser(
        janeId, FullName(FirstName("Janet"), LastName("Doe")), Email("jane.doe@example.com"),
        HashedPassword("hashed")
    )

    private val jpaUserRepository = mock(JpaUserRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaUserRepository.findAll()).thenReturn(listOf(janeEntity, bobEntity))
        `when`(jpaUserRepository.findById(janeId.value)).thenReturn(Optional.of(janeEntity))
        `when`(jpaUserRepository.findById(bobId.value)).thenReturn(Optional.of(bobEntity))
        `when`(jpaUserRepository.existsById(janeId.value)).thenReturn(true)
        `when`(jpaUserRepository.existsById(bobId.value)).thenReturn(true)
        `when`(jpaUserRepository.save(any(UserEntity::class.java))).thenAnswer { it.arguments[0] }
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresUserRepositoryAdapter::class.java)
        context.registerBean(JpaUserRepository::class.java, Supplier { jpaUserRepository })
        context.refresh()
        userRepository = context.getBean(UserRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `the list of the users should not be cached`() {
        // Act
        userRepository.findAll()
        userRepository.findAll()
        // Assert
        verify(jpaUserRepository, times(2)).findAll()
    }

    @Test
    fun `reading a user by id twice should read the table once`() {
        // Act
        userRepository.findById(janeId)
        userRepository.findById(janeId)
        // Assert
        verify(jpaUserRepository, times(1)).findById(janeId.value)
    }

    @Test
    fun `an update should evict the key of the user`() {
        // Arrange
        userRepository.findById(janeId)
        userRepository.save(updatedJane)
        clearInvocations(jpaUserRepository)
        // Act
        userRepository.findById(janeId)
        // Assert
        verify(jpaUserRepository, times(1)).findById(janeId.value)
    }

    @Test
    fun `a delete should evict the key of the user`() {
        // Arrange
        userRepository.findById(janeId)
        userRepository.delete(janeId)
        clearInvocations(jpaUserRepository)
        // Act
        userRepository.findById(janeId)
        // Assert
        verify(jpaUserRepository, times(1)).findById(janeId.value)
    }

    @Test
    fun `an update should keep the other users in the cache`() {
        // Arrange
        userRepository.findById(bobId)
        userRepository.save(updatedJane)
        clearInvocations(jpaUserRepository)
        // Act
        userRepository.findById(bobId)
        // Assert
        verify(jpaUserRepository, never()).findById(bobId.value)
    }

    @Test
    fun `a delete should keep the other users in the cache`() {
        // Arrange
        userRepository.findById(bobId)
        userRepository.delete(janeId)
        clearInvocations(jpaUserRepository)
        // Act
        userRepository.findById(bobId)
        // Assert
        verify(jpaUserRepository, never()).findById(bobId.value)
    }
}
