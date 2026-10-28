package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

// 33 characters, one more than a SKU may have
private const val TOO_LONG_SKU = "A23456789-123456789-123456789-123"

class CreateProductUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val useCase = CreateProductUseCase(productRepository)
    private val readProductUseCase = ReadProductUseCase(productRepository)
    private val updateProductUseCase = UpdateProductUseCase(productRepository)
    private val listProductsUseCase = ListProductsUseCase(productRepository)
    private val proposeSkuUseCase = ProposeSkuUseCase(productRepository)
    private val productUseCases = ProductUseCases(
        useCase,
        readProductUseCase,
        listProductsUseCase,
        updateProductUseCase,
        proposeSkuUseCase
    )
    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val lampSku = Sku("LAMP-DESK")
    private val lamp = DraftProduct(lampSku, Name("Desk lamp"), Description("Warm light"), Category.HOME, lampPrice)
    private val mugPrice = Price(Amount(BigDecimal(12)), Currency.EUR)
    private val mugSku = Sku("MUG-CERAMIC")
    private val mug = DraftProduct(mugSku, Name("Mug"), Description("Ceramic"), Category.HOME, mugPrice)

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
    fun `should store the product with its translations`() {
        // Arrange
        val productId = ProductId.nextId()
        val frenchText = ProductText(Name("Lampe de bureau"), Description("Lumière chaude"))
        val frenchTranslation = ProductTranslation(Language.FR, frenchText)
        val translatedLamp = lamp.copy(translations = lamp.translations.updatedWith(frenchTranslation))
        // Act
        useCase.unfold(CreateProductRequest(productId, translatedLamp))
        // Assert
        val storedLamp = productRepository.findById(productId)!!
        assertThat(storedLamp.translationIn(Language.FR)).isEqualTo(frenchTranslation)
    }

    @Test
    fun `should store the product with its SKU`() {
        // Arrange
        val productId = ProductId.nextId()
        // Act
        useCase.unfold(CreateProductRequest(productId, lamp))
        // Assert
        assertThat(productRepository.findById(productId)?.sku).isEqualTo(lampSku)
    }

    @ParameterizedTest(name = "the SKU \"{0}\" is refused")
    @ValueSource(strings = ["", "lamp", "LAMP DESK", "LAMP--DESK", "-LAMP", "LAMP-", "LAMPE-É", TOO_LONG_SKU])
    fun `should not create a product with a malformed SKU`(malformedSku: String) {
        // Arrange
        val lampWithMalformedSku = lamp.copy(sku = Sku(malformedSku))
        val productId = ProductId.nextId()
        // Act
        val thrown = catchThrowable { useCase.unfold(CreateProductRequest(productId, lampWithMalformedSku)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSkuException::class.java)
        assertThat(productRepository.productIsMissing(productId)).isTrue()
    }

    @Test
    fun `should not create a product with the SKU of another product`() {
        // Arrange
        val lampWithTShirtSku = lamp.copy(sku = Sku("CLO-1"))
        val productId = ProductId.nextId()
        // Act
        val thrown = catchThrowable { useCase.unfold(CreateProductRequest(productId, lampWithTShirtSku)) }
        // Assert
        assertThat(thrown).isInstanceOf(SkuAlreadyUsedException::class.java)
        assertThat(productRepository.productIsMissing(productId)).isTrue()
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
    fun `each creation through the shared product use cases should return its own product`() {
        // Arrange
        val createdLamp = productUseCases.createProduct(lamp)
        // Act
        val createdMug = productUseCases.createProduct(mug)
        // Assert
        assertThat(createdLamp.name).isEqualTo(Name("Desk lamp"))
        assertThat(createdMug.name).isEqualTo(Name("Mug"))
        assertThat(createdMug.productId).isNotEqualTo(createdLamp.productId)
    }
}
