package org.flexstore.domain.product.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class CatalogSearchTest : WithAssertions {

    private fun priceOf(amount: Int) = Price(Amount(BigDecimal(amount)), Currency.EUR)

    private fun productOf(id: String, name: String, category: Category, amount: Int) = DefinedProduct(
        ProductId(Identity(id)), Sku.from(id), Name(name), Description("Described $name"), category, priceOf(amount)
    )

    private val tShirt = productOf("t-shirt", "T-shirt", Category.CLOTHING, 20)
    private val jacket = productOf("jacket", "Jacket", Category.CLOTHING, 80)
    private val phone = productOf("phone", "Phone", Category.ELECTRONICS, 500)
    private val watch = productOf("watch", "Watch", Category.ACCESSORIES, 80)
    private val products = Products(phone, jacket, watch, tShirt)

    private fun searchOf(
        filter: ProductFilter,
        sort: ProductSort = ProductSort.NAME,
        pageRequest: PageRequest = PageRequest.first(),
        language: Language = Language.EN
    ) = CatalogSearch(language, filter, CatalogPaging(sort, pageRequest))

    private fun filterOf(text: String, category: String = "") =
        ProductFilter(ProductSearchText(text), CategoryFilter.of(category))

    @Test
    fun `without words nor category the catalog should give every product sorted by name`() {
        // Arrange
        val catalogSearch = searchOf(ProductFilter.none())
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(jacket, phone, tShirt, watch)
        assertThat(catalogPage.total).isEqualTo(TotalCount(4))
    }

    @ParameterizedTest(name = "the words \"{0}\" find the product {1}")
    @CsvSource("PHONE, phone", "described watch, watch", "clothing, jacket")
    fun `every word searched should be part of the name, the description or the category`(
        text: String, productId: String
    ) {
        // Arrange
        val catalogSearch = searchOf(filterOf(text))
        // Act
        val catalogPage = products.search(catalogSearch)
        val foundProducts = catalogPage.content.asList().map { (it as DefinedProduct).productId.id.value }
        // Assert
        assertThat(foundProducts).contains(productId)
    }

    @Test
    fun `a word that is in no product should find none`() {
        // Arrange
        val catalogSearch = searchOf(filterOf("zeppelin"))
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).isEmpty()
        assertThat(catalogPage.total).isEqualTo(TotalCount(0))
    }

    @Test
    fun `a category should keep its products only`() {
        // Arrange
        val catalogSearch = searchOf(filterOf("", "clothing"))
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(jacket, tShirt)
    }

    @Test
    fun `a category that does not exist should have no product`() {
        // Arrange
        val catalogSearch = searchOf(filterOf("", "spaceships"))
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).isEmpty()
    }

    @Test
    fun `the lowest price should come first, then the name`() {
        // Arrange
        val catalogSearch = searchOf(ProductFilter.none(), ProductSort.PRICE_ASCENDING)
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(tShirt, jacket, watch, phone)
    }

    @Test
    fun `the highest price should come first, then the name`() {
        // Arrange
        val catalogSearch = searchOf(ProductFilter.none(), ProductSort.PRICE_DESCENDING)
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(phone, jacket, watch, tShirt)
    }

    @Test
    fun `a page should give its products and the total of the products that match`() {
        // Arrange
        val secondPage = PageRequest.of(1, 3)
        val catalogSearch = searchOf(ProductFilter.none(), pageRequest = secondPage)
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(watch)
        assertThat(catalogPage.total).isEqualTo(TotalCount(4))
    }

    @Test
    fun `a page after the last one should be empty`() {
        // Arrange
        val farPage = PageRequest.of(5, 20)
        val catalogSearch = searchOf(ProductFilter.none(), pageRequest = farPage)
        // Act
        val catalogPage = products.search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).isEmpty()
        assertThat(catalogPage.total).isEqualTo(TotalCount(4))
    }

    @Test
    fun `the words should be searched in the language of the customer`() {
        // Arrange
        val frenchTranslation = ProductTranslation(Language.FR, Name("Veste"), Description("Une veste chaude"))
        val translations = jacket.translations.updatedWith(frenchTranslation)
        val translatedJacket = jacket.copy(translations = translations)
        val catalogSearch = searchOf(filterOf("veste"), language = Language.FR)
        // Act
        val catalogPage = Products(translatedJacket, phone).search(catalogSearch)
        // Assert
        assertThat(catalogPage.content.asList()).containsExactly(translatedJacket)
    }

    @Test
    fun `a sort that does not exist should be the sort by name`() {
        // Arrange
        val requestedSort = "cheapestFirst"
        // Act
        val sort = ProductSort.of(requestedSort)
        // Assert
        assertThat(sort).isEqualTo(ProductSort.NAME)
    }
}
