package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.flexstore.domain.user.*
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.actor.testCustomerActor

class ProposeSkuUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val administrator = testAdministratorActor
    private val useCase = ProposeSkuUseCase(productRepository)

    @Test
    fun `the SKU proposed should take the first number that no product has`() {
        // Arrange
        val price = Price(Amount(BigDecimal(30)), Currency.EUR)
        val firstMug = DefinedProduct(ProductId(Identity("mug-1")), Name("Mug"), price)
        productRepository.save(firstMug.copy(sku = Sku("HOM-MUG-0001")))
        // Act
        val sku = useCase.unfold(SkuProposalRequest(administrator, SkuBasis(Category.HOME, Words.of("Mug"))))
        // Assert
        assertThat(sku).isEqualTo(Sku("HOM-MUG-0002"))
    }

    @Test
    fun `a customer should not ask for a SKU`() {
        // Arrange
        val request = SkuProposalRequest(testCustomerActor, SkuBasis(Category.HOME, Words.of("Mug")))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductAdministrationForbiddenException::class.java)
    }
}
