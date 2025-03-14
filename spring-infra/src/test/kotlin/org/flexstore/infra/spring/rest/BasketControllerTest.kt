package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.ucop.domain.entity.Name
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class BasketControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var basketController: BasketController

    @Test
    fun `should return empty basket items`() {
        mockMvc.perform(get("/api/basket/items"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `should add item to basket`() {
        val item = createItem()

        mockMvc.perform(
            post("/api/basket/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        )
            .andExpect(status().isCreated)
    }

    private fun createItem(): Item {
        val price1 = Price(Amount(BigDecimal(10)), Currency.EUR)
        val product1 = Product(ProductId(Identity("1")), Name("Product1"), price1)
        return Item(ItemId(Identity("1")), product1, Quantity(1))
    }

}