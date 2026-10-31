package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.invoice.*
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
class SellerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var sellerRepository: SellerRepository

    private lateinit var sellerBefore: Seller

    @BeforeEach
    fun keepSeller() {
        sellerBefore = sellerRepository.current()
    }

    // A new version with the details of the seller before the test, which becomes the current one again
    @AfterEach
    fun restoreSeller() {
        sellerRepository.save(Seller(SellerId.nextId(), sellerBefore.details))
    }

    private fun updateSeller(sellerJson: String) = mockMvc.perform(
        put("/api/admin/seller").with(asAdmin()).contentType(MediaType.APPLICATION_JSON).content(sellerJson)
    )

    @Test
    fun `an admin should read the seller written on the invoices`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/seller").with(asAdmin()))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value(sellerBefore.details.contact.name.value))
    }

    @Test
    fun `an admin should change the seller written on the next invoices`() {
        // Arrange
        val sellerJson = """{"name": "Flexstore Group", "address": "2 avenue du Test, 69001 Lyon, France",
            "siret": "98765432100019", "vatNumber": "FR98765432100"}"""
        // Act
        val response = updateSeller(sellerJson)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.name").value("Flexstore Group"))
        mockMvc.perform(get("/api/admin/seller").with(asAdmin()))
            .andExpect(jsonPath("$.siret").value("98765432100019"))
        assertThat(sellerRepository.findBy(sellerBefore.id)).isEqualTo(sellerBefore)
    }

    @Test
    fun `a seller with a malformed SIRET should be refused`() {
        // Arrange
        val sellerJson = """{"name": "Flexstore", "address": "1 rue de la Démo, 75001 Paris, France",
            "siret": "123", "vatNumber": "FR12345678901"}"""
        // Act
        val response = updateSeller(sellerJson)
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should not read the seller of the administration`() {
        // Act
        val response = mockMvc.perform(get("/api/admin/seller").with(asUser("u-customer")))
        // Assert
        response.andExpect(status().isForbidden)
    }
}
