package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.flexstore.domain.user.*
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.usersOfTheAdministration

class ProposeSkuUseCaseTest : WithAssertions {

    private val productRepository = InMemoryProductRepository()
    private val userRepository = usersOfTheAdministration()
    private val administrator = testAdministrator.id
    private val useCase = ProposeSkuUseCase(productRepository, userRepository)

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
        val request = SkuProposalRequest(testCustomer.id, SkuBasis(Category.HOME, Words.of("Mug")))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductAdministrationForbiddenException::class.java)
    }
}
