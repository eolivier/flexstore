package org.flexstore.infra.spring.rest

import org.flexstore.domain.seller.*
import org.flexstore.domain.settings.ShopSettings
import org.flexstore.domain.settings.ShopSettingsRepository
import org.flexstore.infra.spring.security.asAdmin
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.RequestPostProcessor

@ExtendWith(ExistingCustomer::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SellerSetupIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var sellerRepository: SellerRepository

    @Autowired
    private lateinit var shopSettingsRepository: ShopSettingsRepository

    private lateinit var sellerBefore: Seller

    private val sellerJson = """{"name": "Flexstore Group", "address": "2 avenue du Test, 69001 Lyon, France",
        "siret": "98765432100019", "vatNumber": "FR98765432100", "phone": "04 72 00 00 00",
        "email": "contact@flexstore-group.fr", "website": "https://www.flexstore-group.fr",
        "legalForm": "SARL", "shareCapital": "50 000", "registryCity": "Lyon"}"""

    @BeforeEach
    fun keepSeller() {
        sellerBefore = sellerRepository.current()
        shopSettingsRepository.save(ShopSettings.byDefault())
    }

    @AfterEach
    fun restoreSellerAndSettings() {
        sellerRepository.save(Seller(SellerId.nextId(), sellerBefore.detail))
        shopSettingsRepository.save(ShopSettings.byDefault())
    }

    private fun completeSetup(user: RequestPostProcessor, json: String): ResultActions = mockMvc.perform(
        put("/api/admin/seller/setup").with(user).contentType(MediaType.APPLICATION_JSON).content(json)
    )

    @Test
    fun `the seller setup should be off before the assistant is finished`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.sellerSetup").value("DISABLED"))
    }

    @Test
    fun `an admin finishing the assistant should save the seller and turn the seller setup on`() {
        // Act
        val response = completeSetup(asAdmin(), sellerJson)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.name").value("Flexstore Group"))
        mockMvc.perform(get("/api/admin/seller").with(asAdmin()))
            .andExpect(jsonPath("$.siret").value("98765432100019"))
        mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
            .andExpect(jsonPath("$.sellerSetup").value("ENABLED"))
    }

    @Test
    fun `a refused seller should leave the seller setup off`() {
        // Arrange
        val invalidSellerJson = sellerJson.replace("98765432100019", "123")
        // Act
        val response = completeSetup(asAdmin(), invalidSellerJson)
        // Assert
        response.andExpect(status().isBadRequest)
        mockMvc.perform(get("/api/admin/settings").with(asAdmin()))
            .andExpect(jsonPath("$.sellerSetup").value("DISABLED"))
    }

    @Test
    fun `a customer should not finish the assistant`() {
        // Act
        val response = completeSetup(asUser(EXISTING_CUSTOMER_ID), sellerJson)
        // Assert
        response.andExpect(status().isForbidden)
    }
}
