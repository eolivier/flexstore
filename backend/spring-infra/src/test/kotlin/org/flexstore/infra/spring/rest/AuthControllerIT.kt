package org.flexstore.infra.spring.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.user.service.*
import org.flexstore.domain.user.usecase.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.rest.json.DefinedJsonUser
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonUser
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.flexstore.domain.user.administration.UserService
import org.flexstore.domain.user.administration.usecase.CreateUserRequest

private const val USER_ID = "1"
private const val DRAFT_USER_EMAIL = "draft.user@example.com"

@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var actorResolutionService: ActorResolutionService

    private fun administrator() = actorResolutionService.actorOf(UserId.of(ADMIN_ACCOUNT_ID))

    @AfterEach
    fun cleanUp() {
        userRepository.delete(UserId.of(USER_ID))
        userRepository.findByEmail(Email(DRAFT_USER_EMAIL)).let { userRepository.delete(it.id) }
    }

    @Test
    fun `should change password with the current password`() {
        // Arrange
        userService.createUser(CreateUserRequest(administrator(), createJsonUser().toUser()))
        val request = """{"currentPassword": "testpassword", "newPassword": "new-password"}"""
        // Act
        mockMvc.perform(
            post("/api/users/$USER_ID/password").with(asUser(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isNoContent)
        // Assert
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("new-password")))
            .isTrue()
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("testpassword")))
            .isFalse()
    }

    @Test
    fun `should not change password with a wrong current password`() {
        // Arrange
        userService.createUser(CreateUserRequest(administrator(), createJsonUser().toUser()))
        val request = """{"currentPassword": "wrong-password", "newPassword": "new-password"}"""
        // Act - Assert
        mockMvc.perform(
            post("/api/users/$USER_ID/password").with(asUser(USER_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isBadRequest)
        assertThat(userRepository.passwordMatches(Email("john.doe@example.com"), PlainPassword("testpassword")))
            .isTrue()
    }

    @Test
    fun `should login with valid credentials`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(CreateUserRequest(administrator(), jsonUser.toUser()))
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
        return DefinedJsonUser(USER_ID,"John", "Doe", "john.doe@example.com", "testpassword")
    }

}
