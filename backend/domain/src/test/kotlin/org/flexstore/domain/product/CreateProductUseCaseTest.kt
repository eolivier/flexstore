package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CreateProductUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val useCase = CreateProductUseCase(productRepository)
    private val facade = ProductUseCaseFacade(useCase, ReadProductUseCase(productRepository))
    private val lamp = DraftProduct(Name("Desk lamp"), Description("Warm light"), Category.HOME, Price(Amount(BigDecimal(35)), Currency.EUR))
    private val mug = DraftProduct(Name("Mug"), Description("Ceramic"), Category.HOME, Price(Amount(BigDecimal(12)), Currency.EUR))

    @Test
    fun `should store the product with the given id`() {
        // Arrange
        val productId = ProductId.nextId()
        // Act
        useCase.unfold(CreateProductRequest(productId, lamp))
        // Assert
        assertThat(productRepository.findById(productId)?.name).isEqualTo(Name("Desk lamp"))
    }

    @Test
    fun `should not create a product that already exists`() {
        // Arrange
        useCase.unfold(CreateProductRequest(ProductId.nextId(), lamp))
        // Act
        val thrown = catchThrowable { useCase.unfold(CreateProductRequest(ProductId.nextId(), lamp)) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductAlreadyExists::class.java)
    }

    @Test
    fun `each creation through the shared facade should return its own product`() {
        // Arrange
        val createdLamp = facade.createProduct(lamp)
        // Act
        val createdMug = facade.createProduct(mug)
        // Assert
        assertThat(createdLamp.name).isEqualTo(Name("Desk lamp"))
        assertThat(createdMug.name).isEqualTo(Name("Mug"))
        assertThat(createdMug.productId).isNotEqualTo(createdLamp.productId)
    }
}
