package org.flexstore.infra.spring.rest

import org.flexstore.domain.legal.*
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LegalPageIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var legalPageRepository: LegalPageRepository

    private lateinit var termsOfSaleBefore: LegalPage

    @BeforeEach
    fun keepTermsOfSale() {
        termsOfSaleBefore = legalPageRepository.current(LegalPageKind.TERMS_OF_SALE)
    }

    // A new version with the text of the terms of sale before the test, which becomes the current one again
    @AfterEach
    fun restoreTermsOfSale() {
        legalPageRepository.save(termsOfSaleBefore.copy(id = LegalPageId.nextId()))
    }

    private fun publishTermsOfSale(text: String) = mockMvc.perform(
        put("/api/admin/legal-pages/terms-of-sale").with(asAdmin()).contentType(MediaType.APPLICATION_JSON)
            .content("""{"text": "$text"}""")
    )

    @Test
    fun `a visitor should read the terms of sale published by an admin`() {
        // Arrange
        publishTermsOfSale("Article 1 - Objet\\nLes présentes conditions...").andExpect(status().isOk)
        // Act
        val response = mockMvc.perform(get("/api/legal-pages/terms-of-sale"))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.kind").value("TERMS_OF_SALE"))
            .andExpect(jsonPath("$.text").value("Article 1 - Objet\nLes présentes conditions..."))
            .andExpect(jsonPath("$.publishedAt").isNotEmpty)
    }

    @Test
    fun `a visitor should read the legal notice, even before it is written`() {
        // Act
        val response = mockMvc.perform(get("/api/legal-pages/legal-notice"))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.kind").value("LEGAL_NOTICE"))
    }

    @Test
    fun `an unknown legal page should not be found`() {
        // Act
        val response = mockMvc.perform(get("/api/legal-pages/privacy"))
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `a legal page without text should be refused`() {
        // Act
        val response = publishTermsOfSale("   ")
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should not publish a legal page`() {
        // Act
        val response = mockMvc.perform(
            put("/api/admin/legal-pages/legal-notice").with(asUser("u-customer"))
                .contentType(MediaType.APPLICATION_JSON).content("""{"text": "Mentions"}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a token with the admin role should not publish a legal page for a user who is not an administrator`() {
        // Act
        val response = mockMvc.perform(
            put("/api/admin/legal-pages/legal-notice").with(asAdmin("u-unknown"))
                .contentType(MediaType.APPLICATION_JSON).content("""{"text": "Mentions"}""")
        )
        // Assert
        response.andExpect(status().isForbidden)
    }
}
