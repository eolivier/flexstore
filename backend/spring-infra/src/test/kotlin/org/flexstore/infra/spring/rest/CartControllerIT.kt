package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.valueobject.Category
import org.flexstore.domain.valueobject.Currency
import org.flexstore.infra.spring.adapter.rest.CartController
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CartControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var cartController: CartController

    @Test
    fun `should return empty cart items`() {
        var result = mockMvc.perform(get("/api/cart/cart-items"))
            .andExpect(status().isOk)
            .andReturn()
        assertThat(result.response.contentAsString).isEmpty()
    }

    @Test
    fun `should save item to cart`() {
        val jsonDraftItem = createJsonDraftItem()

        mockMvc.perform(
            post("/api/cart/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jsonDraftItem))
        )
            .andExpect(status().isCreated)
    }

    private fun createJsonDraftItem(): JsonItem {
        return JsonItem(
            productId = "1",
            productName = "Product1",
            productDescription = "Product1 description",
            productCategory = Category.CLOTHING.name,
            productPrice = BigDecimal(10),
            productCurrency =  Currency.EUR.symbol,
            productQuantity = 10
        )
    }
}