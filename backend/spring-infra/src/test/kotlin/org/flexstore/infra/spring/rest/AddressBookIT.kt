package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.databind.ObjectMapper
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartItemRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaCartRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaOrderRepository
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.JpaUserRepository
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.security.asUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

private const val CUSTOMER_ID = "it-address-book-customer"

@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AddressBookIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jpaUserRepository: JpaUserRepository

    @Autowired
    private lateinit var orderRepository: JpaOrderRepository

    @Autowired
    private lateinit var cartItemRepository: JpaCartItemRepository

    @Autowired
    private lateinit var cartRepository: JpaCartRepository

    private val janeDoe = PrivateIndividual(FullName(FirstName("Jane"), LastName("Doe")))
    private val france = Country("France")
    private val homeTown = Town(PostalCode("75011"), City("Paris"))
    private val homeLocation = Location(StreetAddress(Street("12 rue des Lilas")), Locality(homeTown, france))
    private val officeStreet = Street("3 place du Marché")
    private val officeTown = Town(PostalCode("69001"), City("Lyon"))
    private val officeLocation = Location(StreetAddress(officeStreet), Locality(officeTown, france))

    private val acmeAddress = """{"recipientType": "LEGAL_ENTITY", "companyName": "Acme",
        "street": "8 avenue Foch", "postalCode": "13001", "city": "Marseille", "country": "France"}"""

    @BeforeEach
    fun createCustomer() {
        cleanUp()
        val home = PostalAddress(janeDoe, homeLocation)
        val addresses = PostalAddresses(home, PostalAddress(janeDoe, officeLocation))
        val email = Email("it-address-book@example.com")
        val customerId = UserId.ValidUserId(CUSTOMER_ID)
        val customer = User.DefinedUser(customerId, janeDoe.fullName, email, PlainPassword("pwd"))
        userRepository.save(customer.withPostalAddresses(addresses))
    }

    @AfterEach
    fun cleanUp() {
        jdbcTemplate.deleteOrdersOf(CUSTOMER_ID)
        cartRepository.findByUserId(CUSTOMER_ID)?.let { cart ->
            cartItemRepository.deleteAll(cartItemRepository.findAll().filter { it.cartId == cart.id })
            cartRepository.delete(cart)
        }
        if (jpaUserRepository.existsById(CUSTOMER_ID)) jpaUserRepository.deleteById(CUSTOMER_ID)
    }

    @Test
    fun `a customer should see their addresses, in their order`() {
        // Act
        val response = mockMvc.perform(get("/api/accounts/me/addresses").with(asUser(CUSTOMER_ID)))
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[0].city").value("Paris"))
            .andExpect(jsonPath("$[1].city").value("Lyon"))
    }

    @Test
    fun `a customer should change one of their addresses, at the same place`() {
        // Act
        val response = changeFirstAddress(acmeAddress)
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$[0].companyName").value("Acme"))
            .andExpect(jsonPath("$[1].city").value("Lyon"))
    }

    @Test
    fun `an incomplete address should be refused`() {
        // Arrange
        val withoutCity = """{"recipientType": "PRIVATE_INDIVIDUAL", "firstName": "Jane", "lastName": "Doe",
            "street": "8 avenue Foch", "postalCode": "13001", "city": "", "country": "France"}"""
        // Act
        val response = changeFirstAddress(withoutCity)
        // Assert
        response.andExpect(status().isBadRequest)
    }

    @Test
    fun `a customer should remove one of their addresses`() {
        // Act
        val response = removeAddress(0)
        // Assert
        response.andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].city").value("Lyon"))
    }

    @Test
    fun `an address that does not exist should not be found`() {
        // Act
        val response = removeAddress(5)
        // Assert
        response.andExpect(status().isNotFound)
    }

    @Test
    fun `an order should keep its address when the customer changes or removes it`() {
        // Arrange
        val orderId = placeOrderDeliveredHome()
        changeFirstAddress(acmeAddress).andExpect(status().isOk)
        // Act
        removeAddress(0).andExpect(status().isOk)
        // Assert
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.deliveryAddress.city").value("Paris"))
            .andExpect(jsonPath("$.deliveryAddress.firstName").value("Jane"))
    }

    @Test
    fun `a customer should remove an address used by an order, which keeps it`() {
        // Arrange
        val orderId = placeOrderDeliveredHome()
        // Act
        val response = removeAddress(0)
        // Assert
        response.andExpect(status().isOk).andExpect(jsonPath("$.length()").value(1))
        mockMvc.perform(get("/api/orders/$orderId").with(asUser(CUSTOMER_ID)))
            .andExpect(jsonPath("$.deliveryAddress.city").value("Paris"))
    }

    private fun removeAddress(position: Int) =
        mockMvc.perform(delete("/api/accounts/me/addresses/$position").with(asUser(CUSTOMER_ID)))

    private fun changeFirstAddress(address: String) = mockMvc.perform(
        put("/api/accounts/me/addresses/0").with(asUser(CUSTOMER_ID))
            .contentType(MediaType.APPLICATION_JSON).content(address)
    )

    private fun placeOrderDeliveredHome(): String {
        val item = JsonItem("", "6", "Product", "Description", "ELECTRONICS", BigDecimal.TEN, "€", 1)
        mockMvc.perform(
            post("/api/cart/save").with(asUser(CUSTOMER_ID)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item))
        ).andExpect(status().isOk)
        val homeAddresses = """{"deliveryAddress": {"recipientType": "PRIVATE_INDIVIDUAL",
            "firstName": "Jane", "lastName": "Doe",
            "street": "12 rue des Lilas", "postalCode": "75011", "city": "Paris", "country": "France"}}"""
        val placement = post("/api/orders").with(asUser(CUSTOMER_ID))
            .contentType(MediaType.APPLICATION_JSON).content(homeAddresses)
        val body = mockMvc.perform(placement).andExpect(status().isCreated).andReturn().response.contentAsString
        return objectMapper.readTree(body)["id"].asText()
    }
}
