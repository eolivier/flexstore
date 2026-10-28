package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class SkuTest : WithAssertions {

    @Test
    fun `a typed SKU should be trimmed and put in upper case`() {
        // Act
        val sku = Sku.from("  tshirt-organic-m ")
        // Assert
        assertThat(sku).isEqualTo(Sku("TSHIRT-ORGANIC-M"))
    }

    @ParameterizedTest(name = "the SKU \"{0}\" is well formed")
    @ValueSource(strings = ["A", "CLO-1", "TSHIRT-ORGANIC-M", "123", "A23456789-123456789-123456789-12"])
    fun `upper-case letters and digits in groups separated by hyphens should be well formed`(value: String) {
        // Act
        val wellFormed = Sku(value).isWellFormed()
        // Assert
        assertThat(wellFormed).isTrue()
    }

    @ParameterizedTest(name = "a product of {0} named \"{1}\" is first proposed {2}")
    @CsvSource(
        "CLOTHING, Organic Cotton T-shirt, CLO-ORGANIC-COTTON-0001",
        "HOME, Lampe de chevet été, HOM-LAMPE-DE-0001",
        "ELECTRONICS, 10\" Tablet, ELE-10-TABLET-0001",
        "SPORTS, Basketball, SPO-BASKETBA-0001",
        "BOOKS, '', BOO-0001",
    )
    fun `the first SKU proposed should be made of the category and the first words of the name`(
        category: Category,
        name: String,
        proposedSku: String
    ) {
        // Act
        val firstProposal = Sku.proposalsFor(SkuBasis(category, name)).first()
        // Assert
        assertThat(firstProposal).isEqualTo(Sku(proposedSku))
    }

    @Test
    fun `the SKUs proposed should follow each other by their number`() {
        // Act
        val proposals = Sku.proposalsFor(SkuBasis(Category.HOME, "Mug")).take(3).toList()
        // Assert
        assertThat(proposals).containsExactly(Sku("HOM-MUG-0001"), Sku("HOM-MUG-0002"), Sku("HOM-MUG-0003"))
    }

    @Test
    fun `the SKUs proposed should be well formed`() {
        // Arrange
        val skuBasis = SkuBasis(Category.ACCESSORIES, "Sac à dos urbain — édition 2026!")
        // Act
        val firstProposal = Sku.proposalsFor(skuBasis).first()
        // Assert
        assertThat(firstProposal.isWellFormed()).isTrue()
    }
}
