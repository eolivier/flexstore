package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.Description
import org.flexstore.domain.sharedkernel.Name
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ProductTranslationsTest : WithAssertions {

    private val englishText = ProductText(Name("Waterproof Jacket"), Description("Resists rain"))
    private val englishJacket = ProductTranslation(Language.EN, englishText)
    private val frenchText = ProductText(Name("Veste imperméable"), Description("Résiste à la pluie"))
    private val frenchJacket = ProductTranslation(Language.FR, frenchText)
    private val translations = ProductTranslations(englishJacket, frenchJacket)

    @ParameterizedTest(name = "in {0}, the jacket is called {1}")
    @CsvSource("EN, Waterproof Jacket", "FR, Veste imperméable")
    fun `a product should be read in a language it is translated into`(language: Language, name: String) {
        // Act
        val translation = translations.translationIn(language)
        // Assert
        assertThat(translation.name).isEqualTo(Name(name))
    }

    @Test
    fun `a product not translated into a language should be read in the default language`() {
        // Arrange
        val englishOnly = ProductTranslations(englishJacket)
        // Act
        val translation = englishOnly.translationIn(Language.FR)
        // Assert
        assertThat(translation).isEqualTo(englishJacket)
    }

    @Test
    fun `a product should have a translation in the default language`() {
        // Act
        val thrown = catchThrowable { ProductTranslations(frenchJacket) }
        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `a product should have one translation per language at most`() {
        // Arrange
        val otherFrenchText = ProductText(Name("Veste de pluie"), Description("Imperméable"))
        val otherFrenchJacket = ProductTranslation(Language.FR, otherFrenchText)
        // Act
        val thrown = catchThrowable { ProductTranslations(englishJacket, frenchJacket, otherFrenchJacket) }
        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `a translation given should replace the one in its language and keep the others`() {
        // Arrange
        val renamedJacket = ProductTranslation(Language.EN, Name("Rain Jacket"), Description("Resists rain"))
        // Act
        val updatedTranslations = translations.updatedWith(renamedJacket)
        // Assert
        assertThat(updatedTranslations).isEqualTo(ProductTranslations(renamedJacket, frenchJacket))
    }

    @Test
    fun `a translation in a new language should be added`() {
        // Arrange
        val englishOnly = ProductTranslations(englishJacket)
        val givenTranslations = ProductTranslations(englishJacket, frenchJacket)
        // Act
        val updatedTranslations = englishOnly.updatedWith(givenTranslations)
        // Assert
        assertThat(updatedTranslations).isEqualTo(ProductTranslations(englishJacket, frenchJacket))
    }
}
