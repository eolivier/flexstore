package org.flexstore.infra.spring.rest

import org.flexstore.domain.pagecontent.*
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ShopPageContentIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var shopPageContentRepository: ShopPageContentRepository

    @AfterEach
    fun clearContents() {
        ShopPageContentType.entries.forEach {
            shopPageContentRepository.save(ShopPageContent(it, ShopPageContentText.empty()))
        }
    }

    private fun editWith(type: String, text: String, user: RequestPostProcessor) =
        mockMvc.perform(
            put("/api/admin/shop-page-contents/$type").with(user)
                .contentType(MediaType.APPLICATION_JSON).content("""{"text": "$text"}""")
        )

    @Test
    fun `everyone should read the contents, one for each type`() {
        // Act
        val response = mockMvc.perform(get("/api/shop-page-contents"))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.contents.length()").value(ShopPageContentType.entries.size))
    }

    @Test
    fun `an admin should edit a content, then everyone should read it`() {
        // Act
        val response = editWith("meta-key-words", "shop, demo", asAdmin())
        // Assert
        response.andExpect(status().isOk)
        mockMvc.perform(get("/api/shop-page-contents"))
            .andExpect(jsonPath("$.contents[?(@.type == 'META_KEY_WORDS')].text").value("shop, demo"))
    }

    @Test
    fun `a customer should not edit a content`() {
        // Act
        val response = editWith("meta-key-words", "shop", asUser(EXISTING_CUSTOMER_ID))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a token with the admin role should not edit a content for a user who is not an administrator`() {
        // Act
        val response = editWith("meta-key-words", "shop", asAdmin(EXISTING_CUSTOMER_ID))
        // Assert
        response.andExpect(status().isForbidden)
    }

    @Test
    fun `a malformed Google Analytics id should be refused`() {
        // Act
        val response = editWith("google-analytics-id", "G-1;alert(1)", asAdmin())
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `an unknown type should be reported as not found`() {
        // Act
        val response = editWith("unknown", "shop", asAdmin())
        // Assert
        response.andExpect(status().isNotFound)
    }
}
