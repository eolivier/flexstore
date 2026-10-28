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
    private val proposeSkuUseCase = ProposeSkuUseCase(productRepository)
    private val productUseCases =
        ProductUseCases(createProductUseCase, readProductUseCase, useCase, proposeSkuUseCase)

    companion object {
        private val tShirt = clothes.first()

        @JvmStatic
        fun editions() = listOf(
            Arguments.of("SKU", tShirt.copy(sku = Sku("TSHIRT-ORGANIC-M"))),
            Arguments.of("name", tShirt.renamed(Name("Linen T-shirt"))),
            Arguments.of("description", tShirt.describedAs(Description("Light and breathable"))),
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
        useCase.unfold(UpdateProductRequest(tShirt.renamed(Name("Linen T-shirt"))))
        // Assert
        assertThat(productRepository.findAll().map { (it as DefinedProduct).productId }).isEqualTo(catalogIds)
    }

    @Test
    fun `editing a product should store the translations given, and remove the others`() {
        // Arrange
        val frenchText = ProductText(Name("T-shirt en coton bio"), Description("Confortable et écologique"))
        val frenchTranslation = ProductTranslation(Language.FR, frenchText)
        val translatedTShirt = tShirt.copy(translations = tShirt.translations.updatedWith(frenchTranslation))
        productRepository.save(translatedTShirt)
        val englishOnlyTShirt = tShirt.renamed(Name("Linen T-shirt"))
        // Act
        useCase.unfold(UpdateProductRequest(englishOnlyTShirt))
        // Assert
        val storedTShirt = productRepository.findById(tShirt.productId)!!
        assertThat(storedTShirt.translations).isEqualTo(englishOnlyTShirt.translations)
    }

    @Test
    fun `editing a product should store its translation in a new language`() {
        // Arrange
        val frenchText = ProductText(Name("T-shirt en coton bio"), Description("Confortable et écologique"))
        val frenchTranslation = ProductTranslation(Language.FR, frenchText)
        val translatedTShirt = tShirt.copy(translations = tShirt.translations.updatedWith(frenchTranslation))
        // Act
        useCase.unfold(UpdateProductRequest(translatedTShirt))
        // Assert
        val storedTShirt = productRepository.findById(tShirt.productId)!!
        assertThat(storedTShirt.translationIn(Language.FR)).isEqualTo(frenchTranslation)
    }

    @Test
    fun `should not give a product the SKU of another product`() {
        // Arrange
        val jacketSku = clothes[1].sku
        val tShirtWithJacketSku = tShirt.copy(sku = jacketSku)
        // Act
        val thrown = catchThrowable { useCase.unfold(UpdateProductRequest(tShirtWithJacketSku)) }
        // Assert
        assertThat(thrown).isInstanceOf(SkuAlreadyUsedException::class.java)
        assertThat(productRepository.findById(tShirt.productId)?.sku).isEqualTo(tShirt.sku)
    }

    @Test
    fun `should not give a product a malformed SKU`() {
        // Arrange
        val tShirtWithMalformedSku = tShirt.copy(sku = Sku("T SHIRT"))
        // Act
        val thrown = catchThrowable { useCase.unfold(UpdateProductRequest(tShirtWithMalformedSku)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidSkuException::class.java)
        assertThat(productRepository.findById(tShirt.productId)?.sku).isEqualTo(tShirt.sku)
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
        val editedProduct = tShirt.renamed(Name("Linen T-shirt"))
        // Act
        val returnedProduct = productUseCases.updateProduct(editedProduct)
        // Assert
        assertThat(returnedProduct).isEqualTo(editedProduct)
    }
}
