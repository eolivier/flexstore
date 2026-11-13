package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import java.math.BigDecimal
import kotlin.test.Test

class ReadProductUseCaseTest {

    @Test
    fun `should return the product with the given id`() {
        // Arrange
        val productRepository = mockk<ProductRepository>()
        val productId = ProductId(Identity("1"))
        val product = DefinedProduct(productId, Name("Slim Jeans"), Price(Amount(BigDecimal(49)), Currency.EUR))
        every { productRepository.productIsMissing(productId) } returns false
        every { productRepository.findById(productId) } returns product
        // Act
        val reading = ProductReading(unknownUserActor, productId)
        val readProduct = ReadProductUseCase(productRepository).unfold(reading)
        // Assert
        assertThat(readProduct).isEqualTo(product)
    }

    @Test
    fun `should fail if the product does not exist`() {
        // Arrange
        val productRepository = mockk<ProductRepository>()
        val productId = ProductId(Identity("unknown"))
        every { productRepository.productIsMissing(productId) } returns true
        // Act
        val reading = ProductReading(unknownUserActor, productId)
        val thrown = catchThrowable { ReadProductUseCase(productRepository).unfold(reading) }
        // Assert
        assertThat(thrown)
            .hasCauseInstanceOf(ProductNotFoundException::class.java)
            .hasMessage("Product with ID unknown does not exist.")
    }
}
