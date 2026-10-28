package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class ProposeSkuUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val useCase = ProposeSkuUseCase(productRepository)

    @ParameterizedTest(name = "a product of {0} named \"{1}\" is proposed {2}")
    @CsvSource(
        "CLOTHING, Organic Cotton T-shirt, CLO-ORGANIC-COTTON-0001",
        "HOME, Lampe de chevet été, HOM-LAMPE-DE-0001",
        "ELECTRONICS, 10\" Tablet, ELE-10-TABLET-0001",
        "SPORTS, Basketball, SPO-BASKETBA-0001",
        "BOOKS, '', BOO-0001",
    )
    fun `the SKU proposed should be made of the category and the first words of the name`(
        category: Category,
        name: String,
        proposedSku: String
    ) {
        // Act
        val sku = useCase.proposeFor(SkuBasis(category, name))
        // Assert
        assertThat(sku).isEqualTo(Sku(proposedSku))
    }

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

    @Test
    fun `the SKU proposed should be well formed`() {
        // Act
        val sku = useCase.proposeFor(SkuBasis(Category.ACCESSORIES, "Sac à dos urbain — édition 2026!"))
        // Assert
        assertThat(sku.isWellFormed()).isTrue()
    }
}
