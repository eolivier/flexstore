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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.Optional
import java.util.function.Supplier

// The cache of the reads of the user table, with the real adapter behind the Spring cache, without a database
class UserReadCacheTest {

    private val janeEmail = Email("jane.doe@example.com")
    private val bobEmail = Email("bob.martin@example.com")
    private val janeEntity = UserEntity("user-1", "Jane", "Doe", janeEmail.value, "hashed")
    private val bobEntity = UserEntity("user-2", "Bob", "Martin", bobEmail.value, "hashed")

    private val jpaUserRepository = mock(JpaUserRepository::class.java)
    private lateinit var context: AnnotationConfigApplicationContext
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun startSpring() {
        `when`(jpaUserRepository.findByEmail(janeEmail.value)).thenReturn(janeEntity)
        `when`(jpaUserRepository.findByEmail(bobEmail.value)).thenReturn(bobEntity)
        `when`(jpaUserRepository.findAll()).thenReturn(listOf(janeEntity, bobEntity))
        `when`(jpaUserRepository.findById("user-1")).thenReturn(Optional.of(janeEntity))
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("writesOfJane")
    fun `an insert, an update or a delete should forget the user concerned and the list`(
        write: String,
        action: (UserRepository) -> Any
    ) {
        // Arrange
        userRepository.findByEmail(janeEmail)
        userRepository.findAll()
        // Act
        action(userRepository)
        userRepository.findByEmail(janeEmail)
        userRepository.findAll()
        // Assert
        verify(jpaUserRepository, times(2)).findByEmail(janeEmail.value)
        verify(jpaUserRepository, times(2)).findAll()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("writesOfJane")
    fun `an insert, an update or a delete should keep the other users in the cache`(
        write: String,
        action: (UserRepository) -> Any
    ) {
        // Arrange
        userRepository.findByEmail(bobEmail)
        // Act
        action(userRepository)
        userRepository.findByEmail(bobEmail)
        // Assert
        verify(jpaUserRepository, times(1)).findByEmail(bobEmail.value)
    }

    @Test
    fun `an update that changes the email should forget the previous email`() {
        // Arrange
        userRepository.findByEmail(janeEmail)
        val newEmail = Email("jane.smith@example.com")
        val marriedJane = janeWith(newEmail)
        // Act
        userRepository.save(marriedJane)
        userRepository.findByEmail(janeEmail)
        // Assert
        verify(jpaUserRepository, times(2)).findByEmail(janeEmail.value)
    }

    companion object {
        private fun janeWith(email: Email) = User.DefinedUser(
            UserId.ValidUserId("user-1"), FullName(FirstName("Jane"), LastName("Doe")), email, HashedPassword("hashed")
        )

        private val newcomer = User.of(
            FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("secret123")
        )

        @JvmStatic
        fun writesOfJane() = listOf(
            Arguments.of("an insert", { repository: UserRepository -> repository.save(newcomer) }),
            Arguments.of("an update", { repository: UserRepository ->
                repository.save(janeWith(Email("jane.doe@example.com")))
            }),
            Arguments.of("a delete", { repository: UserRepository -> repository.delete(UserId.ValidUserId("user-1")) })
        )
    }
}
