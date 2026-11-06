package org.flexstore.infra.spring.rest

import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest(properties = ["FLEXSTORE_FRONT_URL=https://shop.example.com", "FLEXSTORE_MAIL_PASSWORD=s3cret-mail"])
@AutoConfigureMockMvc
class AdminEnvironmentIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `an admin should read a plain variable with its value`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/environment").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.name == 'FLEXSTORE_FRONT_URL')].status").value("DEFINED"))
            .andExpect(jsonPath("$[?(@.name == 'FLEXSTORE_FRONT_URL')].value").value("https://shop.example.com"))
    }

    @Test
    fun `an admin should read a defined secret without its value`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/environment").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.name == 'FLEXSTORE_MAIL_PASSWORD')].status").value("DEFINED"))
            .andExpect(jsonPath("$[?(@.name == 'FLEXSTORE_MAIL_PASSWORD')].value").isEmpty)
            .andExpect(content().string(not(containsString("s3cret-mail"))))
    }

    @Test
    fun `an admin should read a variable that is not set as undefined`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/environment").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.name == 'FLEXSTORE_STRIPE_SECRET_KEY')].status").value("UNDEFINED"))
    }

    @Test
    fun `a customer should not read the environment variables`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/environment").with(asUser(EXISTING_CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a visitor should not read the environment variables`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/environment"))
        // Assert
        response.andExpect(status().isUnauthorized)
    }
}
