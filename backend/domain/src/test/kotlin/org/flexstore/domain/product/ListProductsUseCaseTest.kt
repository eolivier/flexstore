package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.accessories
import org.flexstore.infra.repository.clothes
import org.flexstore.infra.repository.electronics
import org.junit.jupiter.api.Test

class ListProductsUseCaseTest : WithAssertions {

    @Test
    fun `the products of the catalog should be listed in its order`() {
        // Arrange
        val useCase = ListProductsUseCase(InMemoryProductRepository())
        // Act
        val products = useCase.unfold(Unit)
        // Assert
        assertThat(products).isEqualTo(Products(clothes + electronics + accessories))
    }
}
