package org.flexstore.domain.product.model

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.TotalCount

// The products of the catalog, in the order of the catalog
data class Products(private val products: List<Product>) {

    constructor(vararg products: Product) : this(products.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<Product> = products.toList()

    // The page of the products that match the search, in its order, with the number of products that match
    fun search(catalogSearch: CatalogSearch): Page<Products> {
        val language = catalogSearch.language
        val matchingProducts = products.filterIsInstance<DefinedProduct>()
            .filter { product -> catalogSearch.filter.accepts(product, language) }
            .sortedWith(catalogSearch.paging.sort.comparator(language))
        val pageProducts = matchingProducts.drop(firstIndexOf(catalogSearch.paging.pageRequest))
            .take(catalogSearch.paging.pageRequest.size.value)
        return Page(Products(pageProducts), TotalCount(matchingProducts.size.toLong()))
    }

    private fun firstIndexOf(pageRequest: PageRequest) = pageRequest.number.value * pageRequest.size.value
}

// The ids of some products of the catalog
data class ProductIds(private val productIds: List<ProductId>) {

    constructor(vararg productIds: ProductId) : this(productIds.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<ProductId> = productIds.toList()
}
