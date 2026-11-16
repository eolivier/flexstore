package org.flexstore.domain.product.model

import org.flexstore.domain.sharedkernel.PageRequest

// The words a customer types to find products: each word is part of the name, the description or the category of
// the product, whatever the case
data class ProductSearchText(val value: String) {

    // Read-only view for the adapters that translate the search: the words, in lower case
    fun asWords(): List<String> = value.lowercase().split(WORD_SEPARATOR).filter { word -> word.isNotBlank() }

    fun matches(product: DefinedProduct, language: Language): Boolean {
        val translation = product.translations.translationIn(language)
        val searchedText = "${translation.name.value} ${translation.description.value} ${product.category.name}"
        val searchedWords = searchedText.lowercase()
        return asWords().all { word -> searchedWords.contains(word) }
    }

    companion object {
        private val WORD_SEPARATOR = Regex("\\s+")

        fun none() = ProductSearchText("")
    }
}

// The categories a customer looks in: all of them, one, or one that the shop does not know (which has no product)
sealed interface CategoryFilter {
    fun accepts(product: DefinedProduct): Boolean

    data object AnyCategory : CategoryFilter {
        override fun accepts(product: DefinedProduct) = true
    }

    data class OnlyCategory(val category: Category) : CategoryFilter {
        override fun accepts(product: DefinedProduct) = product.category == category
    }

    data object UnknownCategory : CategoryFilter {
        override fun accepts(product: DefinedProduct) = false
    }

    companion object {
        // As asked by a client: no category is all of them, a category that does not exist has no product
        fun of(requested: String): CategoryFilter {
            if (requested.isBlank()) return AnyCategory
            val category = Category.entries.firstOrNull { it.name.equals(requested.trim(), ignoreCase = true) }
            return if (category == null) UnknownCategory else OnlyCategory(category)
        }
    }
}

// The order of the products of the catalog: by name, or by the price to pay now, the lowest or the highest first
enum class ProductSort(val code: String) {
    NAME("name"),
    PRICE_ASCENDING("priceAsc"),
    PRICE_DESCENDING("priceDesc");

    fun comparator(language: Language): Comparator<DefinedProduct> {
        val byName = compareBy<DefinedProduct, String>(String.CASE_INSENSITIVE_ORDER) { product ->
            product.translations.translationIn(language).name.value
        }
        val byPrice = compareBy<DefinedProduct> { product -> product.currentPrice.amount.value }
        return when (this) {
            NAME -> byName
            PRICE_ASCENDING -> byPrice.then(byName)
            PRICE_DESCENDING -> byPrice.reversed().then(byName)
        }
    }

    companion object {
        // As asked by a client: a sort that does not exist is the sort by name
        fun of(requested: String) = entries.firstOrNull { it.code == requested } ?: NAME
    }
}

// What a customer keeps of the catalog: the products that match the words and the category
data class ProductFilter(val searchText: ProductSearchText, val categoryFilter: CategoryFilter) {

    fun accepts(product: DefinedProduct, language: Language) =
        categoryFilter.accepts(product) && searchText.matches(product, language)

    companion object {
        fun none() = ProductFilter(ProductSearchText.none(), CategoryFilter.AnyCategory)
    }
}

// How the products kept are given: in this order, a page at a time
data class CatalogPaging(val sort: ProductSort, val pageRequest: PageRequest)

// The page of the catalog that a customer asks for, in their language: the name and the description are read, and
// searched, in it (in the default language for a product not translated into it)
data class CatalogSearch(val language: Language, val filter: ProductFilter, val paging: CatalogPaging)
