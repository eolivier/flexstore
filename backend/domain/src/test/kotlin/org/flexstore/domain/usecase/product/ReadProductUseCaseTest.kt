package org.flexstore.domain.usecase.product

import io.mockk.every
import io.mockk.mockk
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.Test

class ReadProductUseCaseTest {

    @Test
    fun `should return the product with the given id`() {
        // Arrange
        val productRepository = mockk<ProductRepository>()
        val productId = ProductId(Identity("1"))
        val product = DefinedProduct(productId, Name("Slim Jeans"), Price(Amount(BigDecimal(49)), Currency.EUR))
        every { productRepository.findById(productId) } returns product
        // Act
        val readProduct = ReadProductUseCase(productRepository).getProduct(productId)
        // Assert
        assertEquals(product, readProduct)
    }

    @Test
    fun `should fail if the product does not exist`() {
        // Arrange
        val productRepository = mockk<ProductRepository>()
        val productId = ProductId(Identity("unknown"))
        every { productRepository.findById(productId) } returns null
        // Act
        val exception = assertThrows<ProductNotFoundException> {
            ReadProductUseCase(productRepository).getProduct(productId)
        }
        // Assert
        assertEquals("Product with ID unknown does not exist.", exception.message)
    }
}
