package org.flexstore.domain.product.usecase

import org.flexstore.domain.actor.verifyCanRead
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.Page
import org.ucop.domain.entity.*

// A visitor or a customer asks for a page of the catalog
data class CatalogReading(val reader: Actor, val catalogSearch: CatalogSearch)

// A page of the products of the catalog that match the words and the category, in the order asked
class SearchCatalogUseCase(private val productRepository: ProductRepository) :
    QueryUseCase<CatalogReading, Page<Products>> {

    override fun getPreConditions(): PreConditions<CatalogReading> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<CatalogReading, Page<Products>> { catalogReading ->
        Products(productRepository.findAll()).search(catalogReading.catalogSearch)
    }

    override fun getPostConditions(): PostConditions<Page<Products>> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<CatalogReading> { catalogReading ->
        catalogReading.reader.verifyCanRead("the catalog", ::ProductReadingForbiddenException)
    }
}
