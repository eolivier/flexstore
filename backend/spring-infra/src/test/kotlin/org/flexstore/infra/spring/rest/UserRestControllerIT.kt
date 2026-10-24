package org.flexstore.infra.spring.rest

import org.flexstore.domain.user.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonUser
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.flexstore.infra.spring.security.ADMIN_ACCOUNT_ID
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

    @ParameterizedTest(name = "role sent: {0} -> user created as {1}")
    @CsvSource("ADMIN, ADMIN", "USER, USER", ", USER")
    fun `should create the user with the chosen role`(role: String?, expectedRole: String) {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword", role)
        // Act
        val response = mockMvc.perform(
            post("/api/users").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(draftJsonUser))
        )
        // Assert
        response.andExpect(status().isCreated)
            .andExpect(jsonPath("$.role").value(expectedRole))
        assertThat((userRepository.findByEmail(Email(DRAFT_USER_EMAIL)) as User.DefinedUser).role).isEqualTo(Role.valueOf(expectedRole))
    }

    @Test
    fun `should refuse a creation by a token that is not the one of an administrator in the users`() {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword")
        // Act
        val response = mockMvc.perform(
            post("/api/users").with(asAdmin("not-a-user")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(draftJsonUser))
        )
        // Assert
        response.andExpect(status().isForbidden)
        assertThat(userRepository.findByEmail(Email(DRAFT_USER_EMAIL))).isNotInstanceOf(User.DefinedUser::class.java)
    }

    @Test
    fun `should not create a user with an unknown role`() {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword", "SUPERVISOR")
        // Act
        val response = mockMvc.perform(
            post("/api/users").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(draftJsonUser))
        )
        // Assert
        response.andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should not create user with an already used email`() {
        // Arrange
        val draftJsonUser = DraftJsonUser("Draft User", DRAFT_USER_EMAIL, "testpassword")
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), draftJsonUser.toUser()))
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
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
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
    fun `an administrator should edit the name, the email and the role of a user`() {
        // Arrange
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
        val editedUser = EditedJsonUser("Jane Doe", "jane.doe@example.com", "ADMIN")
        // Act
        val response = mockMvc.perform(
            put("/api/users/$USER_ID").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(editedUser))
        )
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Jane Doe"))
            .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    fun `an edit should keep the password, even if one is sent`() {
        // Arrange
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
        val editedUserWithPassword = """{"name": "Jane Doe", "email": "jane.doe@example.com", "password": "another-password"}"""
        // Act
        mockMvc.perform(
            put("/api/users/$USER_ID").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(editedUserWithPassword)
        ).andExpect(status().isOk)
        // Assert
        assertThat(userRepository.passwordMatches(Email("jane.doe@example.com"), PlainPassword("testpassword"))).isTrue()
    }

    @Test
    fun `should refuse an edit by a token that is not the one of an administrator in the users`() {
        // Arrange
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
        val editedUser = EditedJsonUser("Jane Doe", "jane.doe@example.com", "USER")
        // Act
        val response = mockMvc.perform(
            put("/api/users/$USER_ID").with(asAdmin("not-a-user")).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(editedUser))
        )
        // Assert
        response.andExpect(status().isForbidden)
        assertThat((userService.readUser(UserId.of(USER_ID)) as User.DefinedUser).name.value).isEqualTo("John Doe")
    }

    @Test
    fun `should refuse the email of another user`() {
        // Arrange
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
        val editedUser = EditedJsonUser("John Doe", "admin@flexstore.com", "USER")
        // Act
        val response = mockMvc.perform(
            put("/api/users/$USER_ID").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(editedUser))
        )
        // Assert
        response.andExpect(status().isConflict)
    }

    @Test
    fun `should change password with the current password`() {
        // Arrange
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
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
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), createJsonUser().toUser()))
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
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), jsonUser.toUser()))
        // Act - Assert
        mockMvc.perform(delete("/api/users/$USER_ID").with(asAdmin()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should login with valid credentials`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(CreateUserRequest(UserId.of(ADMIN_ACCOUNT_ID), jsonUser.toUser()))
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