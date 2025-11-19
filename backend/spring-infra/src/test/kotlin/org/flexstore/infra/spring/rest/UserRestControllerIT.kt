package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.of
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.service.UserService
import org.flexstore.infra.spring.adapter.rest.json.JsonUser
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

private const val USER_ID = "1"

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
    }

    @Test
    fun `should create user`() {
        // Arrange
        val jsonUser = createJsonUser()
        // Act - Assert
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jsonUser))
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `should return user by id`() {
        // Arrange
        userService.createUser(createJsonUser().toUser())
        val jsonUser = createJsonUser()
        val user = jsonUser.toUser()
        // Act - Assert
        mockMvc.perform(get("/api/users/$USER_ID"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(user)))
    }

    @Test
    fun `should update user`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(jsonUser.toUser())
        // Act - Assert
        val updatedJsonUser = JsonUser("1", "Jane Doe", "jane.doe@example.com", "testpassword")
        mockMvc.perform(
            put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedJsonUser))
        )
            .andExpect(status().isOk)
        val readUser = userService.readUser(UserId.of(USER_ID))
        assertThat(readUser).isEqualTo(updatedJsonUser.toUser())
    }

    @Test
    fun `should delete user`() {
        // Arrange
        val jsonUser = createJsonUser()
        userService.createUser(jsonUser.toUser())
        // Act - Assert
        mockMvc.perform(delete("/api/users/$USER_ID"))
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

    private fun createJsonUser(): JsonUser {
        return JsonUser(USER_ID,"John Doe", "john.doe@example.com", "testpassword")
    }
}