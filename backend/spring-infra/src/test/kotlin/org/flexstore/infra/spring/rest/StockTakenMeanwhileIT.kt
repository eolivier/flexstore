package org.flexstore.infra.spring.rest

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.StockRepository
import org.flexstore.domain.stock.StockReservation
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val CUSTOMER_ID = "it-stock-taken-customer"
private const val LAPTOP_ID = "6"
private const val HEADPHONES_ID = "9"

// Another order takes the last headphones between the check of the stock and their reservation: the order is
// refused, and nothing is kept of it (neither the order nor the laptop already reserved)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StockTakenMeanwhileIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @SpyBean
    private lateinit var stockRepository: StockRepository

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var userRepository: JpaUserRepository

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        jdbcTemplate.refillStocks()
        userRepository.save(UserEntity(CUSTOMER_ID, "Jane", "Doe", "it-stock-taken@example.com", "not-used"))
    }

    @AfterEach
    fun cleanUp() {
        orderRepository.deleteAll(orderRepository.findAllByUserId(CUSTOMER_ID))
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        userRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `an order whose units were taken meanwhile should be refused, and nothing kept of it`() {
        // Arrange
        addToCart(LAPTOP_ID)
        addToCart(HEADPHONES_ID)
        val headphonesId = ProductId(Identity(HEADPHONES_ID))
        doReturn(StockReservation.NOT_COVERED).`when`(stockRepository).reserve(headphonesId, Quantity(1))
        // Act
        val response = mockMvc.perform(post("/api/orders").withHomeAddresses().with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isConflict)
        assertThat(orderRepository.findAllByUserId(CUSTOMER_ID)).isEmpty()
        assertThat(stockOf(LAPTOP_ID)).isEqualTo(100)
    }

    private fun stockOf(productId: String) =
        jdbcTemplate.queryForObject("SELECT quantity FROM stock WHERE product_id = ?", Int::class.java, productId)

    private fun addToCart(productId: String) {
        val item = """{"itemId": "", "productId": "$productId", "productName": "Product",
            "productDescription": "Description", "productCategory": "ELECTRONICS", "productPrice": 10,
            "productCurrency": "€", "productQuantity": 1}"""
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON).content(item)
        ).andExpect(status().isOk)
    }
}
