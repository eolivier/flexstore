package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.SystemActor
import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.sharedkernel.Name
import org.ucop.domain.entity.Actor
import org.flexstore.domain.product.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.model.*
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
        val products = useCase.unfold(unknownUserActor)
        // Assert
        assertThat(products).isEqualTo(Products(clothes + electronics + accessories))
    }

    @Test
    fun `the system should list the products of the catalog`() {
        // Arrange
        val useCase = ListProductsUseCase(InMemoryProductRepository())
        // Act
        val products = useCase.unfold(SystemActor())
        // Assert
        assertThat(products).isEqualTo(Products(clothes + electronics + accessories))
    }

    @Test
    fun `an actor unknown to the shop should not list the products of the catalog`() {
        // Arrange
        val useCase = ListProductsUseCase(InMemoryProductRepository())
        val stranger = Actor(Name("Stranger"))
        // Act
        val thrown = catchThrowable { useCase.unfold(stranger) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductReadingForbiddenException::class.java)
    }
}
