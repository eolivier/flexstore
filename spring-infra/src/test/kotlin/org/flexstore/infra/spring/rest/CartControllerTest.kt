package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.domain.valueobject.Category
import org.flexstore.domain.valueobject.Currency
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
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var cartController: CartController

    @Test
    fun `should return empty cart items`() {
        mockMvc.perform(get("/api/cart/items"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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