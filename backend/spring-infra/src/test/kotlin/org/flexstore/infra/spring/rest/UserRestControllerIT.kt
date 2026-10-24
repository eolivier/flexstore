package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.HashedPassword
import org.flexstore.domain.entity.PlainPassword
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.of
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.service.UserService
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

private const val USER_ID = "1"
private const val DRAFT_USER_EMAIL = "draft.user@example.com"

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    fun cleanUp() {
        userRepository.delete(UserId.of(USER_ID))
        userRepository.findByEmail(Email(DRAFT_USER_EMAIL)).let { userRepository.delete(it.id) }
    }

    @Test
    fun `should create user`() {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword")
        // Act
        mockMvc.perform(
            post("/api/users").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draftJsonUser))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.password").doesNotExist())
        // Assert
        val savedUser = userRepository.findByEmail(Email(DRAFT_USER_EMAIL))
        assertThat(savedUser).isInstanceOf(User.DefinedUser::class.java)
        assertThat((savedUser as User.DefinedUser).password).isInstanceOf(HashedPassword::class.java)
    }

    @Test
    fun `should not create user with an already used email`() {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword")
        userService.createUser(draftJsonUser.toUser())
        // Act - Assert
        mockMvc.perform(
            post("/api/users").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(draftJsonUser))
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `should return user by id`() {
        // Arrange
        userService.createUser(createJsonUser().toUser())
        // Act - Assert
        mockMvc.perform(get("/api/users/$USER_ID").with(asAdmin()))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(USER_ID))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    fun `should update user`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(jsonUser.toUser())
        // Act - Assert
        val updatedJsonUser = DefinedJsonUser("1", "Jane Doe", "jane.doe@example.com", "testpassword")
        mockMvc.perform(
            put("/api/users/1").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJsonUser))
        )
            .andExpect(status().isOk)
        val readUser = userService.readUser(UserId.of(USER_ID)) as User.DefinedUser
        assertThat(readUser.name.value).isEqualTo("Jane Doe")
        assertThat(readUser.email.value).isEqualTo("jane.doe@example.com")
        // The password is stored hashed, so it is checked through the repository
        assertThat(userRepository.passwordMatches(readUser.email, PlainPassword("testpassword"))).isTrue()
    }

    @Test
    fun `should keep password when updating user without password`() {
        // Arrange
        userService.createUser(createJsonUser().toUser())
        val updatedUser = """{"id": "$USER_ID", "name": "Jane Doe", "email": "jane.doe@example.com"}"""
        // Act
        mockMvc.perform(
            put("/api/users/$USER_ID").with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUser)
        )
            .andExpect(status().isOk)
        // Assert
        assertThat(userRepository.passwordMatches(Email("jane.doe@example.com"), PlainPassword("testpassword"))).isTrue()
    }

    @Test
    fun `should change password with the current password`() {
        // Arrange
        userService.createUser(createJsonUser().toUser())
        val request = """{"currentPassword": "testpassword", "newPassword": "new-password"}"""
        // Act
        mockMvc.perform(
            post("/api/users/$USER_ID/password").with(asUser(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isNoContent)
        // Assert
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("new-password"))).isTrue()
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("testpassword"))).isFalse()
    }

    @Test
    fun `should not change password with a wrong current password`() {
        // Arrange
        userService.createUser(createJsonUser().toUser())
        val request = """{"currentPassword": "wrong-password", "newPassword": "new-password"}"""
        // Act - Assert
        mockMvc.perform(
            post("/api/users/$USER_ID/password").with(asUser(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isBadRequest)
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("testpassword"))).isTrue()
    }

    @Test
    fun `should delete user`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(jsonUser.toUser())
        // Act - Assert
        mockMvc.perform(delete("/api/users/$USER_ID").with(asAdmin()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should login with valid credentials`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(jsonUser.toUser())
        val loginRequest = """{"email": "john.doe@example.com", "password": "testpassword"}"""
        // Act - Assert
        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    private fun createJsonUser(): DefinedJsonUser {
        return DefinedJsonUser(USER_ID,"John Doe", "john.doe@example.com", "testpassword")
    }
}