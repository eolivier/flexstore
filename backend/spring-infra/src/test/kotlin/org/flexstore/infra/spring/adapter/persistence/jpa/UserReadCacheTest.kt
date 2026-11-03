package org.flexstore.infra.spring.adapter.persistence.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.PostgresUserRepositoryAdapter
import org.flexstore.infra.spring.config.CacheConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.Optional

// The cache of the reads of the user table, with the real adapter behind the Spring cache, without a database
class UserReadCacheTest {

    private val janeEmail = Email("jane.doe@example.com")
    private val janeEntity = UserEntity("user-1", "Jane", "Doe", janeEmail.value, "hashed")

    private val jpaUserRepository = mock(JpaUserRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaUserRepository.findByEmail(janeEmail.value)).thenReturn(janeEntity)
        `when`(jpaUserRepository.findAll()).thenReturn(listOf(janeEntity))
        `when`(jpaUserRepository.existsById("user-1")).thenReturn(true)
        `when`(jpaUserRepository.findById("user-1")).thenReturn(Optional.of(janeEntity))
        `when`(jpaUserRepository.save(any(UserEntity::class.java))).thenAnswer { it.arguments[0] }
        context = AnnotationConfigApplicationContext()
        context.register(CacheConfig::class.java, PostgresUserRepositoryAdapter::class.java)
        context.registerBean(JpaUserRepository::class.java, { jpaUserRepository })
        context.refresh()
        userRepository = context.getBean(UserRepository::class.java)
    }

    @AfterEach
    fun stopSpring() {
        context.close()
    }

    @Test
    fun `reading the users twice should read the table once`() {
        // Act
        userRepository.findAll()
        userRepository.findAll()
        // Assert
        verify(jpaUserRepository, times(1)).findAll()
    }

    @Test
    fun `reading a user by email twice should read the table once`() {
        // Act
        userRepository.findByEmail(janeEmail)
        userRepository.findByEmail(janeEmail)
        // Assert
        verify(jpaUserRepository, times(1)).findByEmail(janeEmail.value)
    }

    @ParameterizedTest(name = "{0} should empty the cache")
    @MethodSource("writes")
    fun `an insert, an update or a delete should empty the cache`(name: String, write: (UserRepository) -> Any) {
        // Arrange
        userRepository.findAll()
        // Act
        write(userRepository)
        val users = userRepository.findAll()
        // Assert
        assertThat(users).hasSize(1)
        verify(jpaUserRepository, times(2)).findAll()
    }

    companion object {
        private val newcomer = User.of(
            FullName(FirstName("John"), LastName("Smith")), Email("john.smith@example.com"), PlainPassword("secret123")
        )
        private val updatedJane = User.DefinedUser(
            UserId.ValidUserId("user-1"), FullName(FirstName("Janet"), LastName("Doe")),
            Email("jane.doe@example.com"), HashedPassword("hashed")
        )

        @JvmStatic
        fun writes() = listOf(
            org.junit.jupiter.params.provider.Arguments.of("an insert", { repository: UserRepository ->
                repository.save(newcomer)
                Unit
            }),
            org.junit.jupiter.params.provider.Arguments.of("an update", { repository: UserRepository ->
                repository.save(updatedJane)
                Unit
            }),
            org.junit.jupiter.params.provider.Arguments.of("a delete", { repository: UserRepository ->
                repository.delete(UserId.ValidUserId("user-1"))
                Unit
            })
        )
    }
}
