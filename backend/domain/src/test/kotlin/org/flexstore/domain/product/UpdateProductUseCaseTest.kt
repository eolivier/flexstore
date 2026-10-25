package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.clothes
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal

class UpdateProductUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val useCase = UpdateProductUseCase(productRepository)
    private val readProductUseCase = ReadProductUseCase(productRepository)
    private val createProductUseCase = CreateProductUseCase(productRepository)
    private val productUseCases = ProductUseCases(createProductUseCase, readProductUseCase, useCase)

    companion object {
        private val tShirt = clothes.first()

        @JvmStatic
        fun editions() = listOf(
            Arguments.of("name", tShirt.copy(name = Name("Linen T-shirt"))),
            Arguments.of("description", tShirt.copy(description = Description("Light and breathable"))),
            Arguments.of("category", tShirt.copy(category = Category.SPORTS)),
            Arguments.of("price", tShirt.copy(price = Price(Amount(BigDecimal(29)), Currency.EUR))),
        )
    }

    @ParameterizedTest(name = "should store the edited {0}")
    @MethodSource("editions")
    fun `should store the edited product`(@Suppress("UNUSED_PARAMETER") editedField: String, editedProduct: DefinedProduct) {
        // Act
        useCase.unfold(UpdateProductRequest(editedProduct))
        // Assert
        assertThat(productRepository.findById(tShirt.productId)).isEqualTo(editedProduct)
    }

    @Test
    fun `should keep the edited product at its place in the catalog`() {
        // Arrange
        val catalogIds = productRepository.findAll().map { (it as DefinedProduct).productId }
        // Act
        useCase.unfold(UpdateProductRequest(tShirt.copy(name = Name("Linen T-shirt"))))
        // Assert
        assertThat(productRepository.findAll().map { (it as DefinedProduct).productId }).isEqualTo(catalogIds)
    }

    @Test
    fun `should not edit an unknown product`() {
        // Arrange
        val unknownProduct = tShirt.copy(productId = ProductId(Identity("unknown")))
        // Act
        val thrown = catchThrowable { useCase.unfold(UpdateProductRequest(unknownProduct)) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
        assertThat(productRepository.findById(unknownProduct.productId)).isNull()
    }

    @Test
    fun `the product use cases should return the edited product`() {
        // Arrange
        val editedProduct = tShirt.copy(name = Name("Linen T-shirt"))
        // Act
        val returnedProduct = productUseCases.updateProduct(editedProduct)
        // Assert
        assertThat(returnedProduct).isEqualTo(editedProduct)
    }
}
