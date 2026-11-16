package org.flexstore.domain.product.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.SystemActor
import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.clothes
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Actor

class SearchCatalogUseCaseTest : WithAssertions {

    private val useCase = SearchCatalogUseCase(InMemoryProductRepository())
    private val clothingFilter = ProductFilter(ProductSearchText.none(), CategoryFilter.OnlyCategory(Category.CLOTHING))
    private val catalogPaging = CatalogPaging(ProductSort.NAME, PageRequest.first())
    private val catalogSearch = CatalogSearch(Language.EN, clothingFilter, catalogPaging)

    @Test
    fun `a visitor should read the page of the catalog that matches the search`() {
        // Arrange
        val catalogReading = CatalogReading(unknownUserActor, catalogSearch)
        // Act
        val catalogPage = useCase.unfold(catalogReading)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactlyInAnyOrderElementsOf(clothes)
        assertThat(catalogPage.total).isEqualTo(TotalCount(clothes.size.toLong()))
    }

    @Test
    fun `the system should read the page of the catalog`() {
        // Arrange
        val catalogReading = CatalogReading(SystemActor(), catalogSearch)
        // Act
        val catalogPage = useCase.unfold(catalogReading)
        // Assert
        assertThat(catalogPage.total).isEqualTo(TotalCount(clothes.size.toLong()))
    }

    @Test
    fun `an actor unknown to the shop should not read the catalog`() {
        // Arrange
        val stranger = Actor(Name("Stranger"))
        val catalogReading = CatalogReading(stranger, catalogSearch)
        // Act
        val thrown = catchThrowable { useCase.unfold(catalogReading) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductReadingForbiddenException::class.java)
    }
}
