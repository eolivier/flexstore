package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.rest.ProductController
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class DefinedProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var productController: ProductController

    @Test
    fun `should returnØ products`() {
        val mvcResult = mockMvc.perform(get("/api/products/"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
        val responseContent = mvcResult.response.contentAsString
        val products: List<JsonProduct> = objectMapper.readValue(
            responseContent,
            objectMapper.typeFactory.constructCollectionType(List::class.java, JsonProduct::class.java)
        )
        assertThat(products).isNotEmpty
    }
}