package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProposeSkuUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val useCase = ProposeSkuUseCase(productRepository)

    @Test
    fun `the SKU proposed should take the first number that no product has`() {
        // Arrange
        val price = Price(Amount(BigDecimal(30)), Currency.EUR)
        val firstMug = DefinedProduct(ProductId(Identity("mug-1")), Name("Mug"), price)
        productRepository.save(firstMug.copy(sku = Sku("HOM-MUG-0001")))
        // Act
        val sku = useCase.proposeFor(SkuBasis(Category.HOME, "Mug"))
        // Assert
        assertThat(sku).isEqualTo(Sku("HOM-MUG-0002"))
    }
}
