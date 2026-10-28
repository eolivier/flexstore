package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test

class WordsTest : WithAssertions {

    @Test
    fun `the words of a text should be in upper case, without accents nor other signs`() {
        // Act
        val words = Words.of("T-shirt d'été, bio!")
        // Assert
        assertThat(words).isEqualTo(Words(Word("T"), Word("SHIRT"), Word("D"), Word("ETE"), Word("BIO")))
    }

    @Test
    fun `the first words should be shortened to their first letters`() {
        // Arrange
        val words = Words(Word("BASKETBALL"), Word("SHOES"), Word("PRO"))
        // Act
        val shortenedWords = words.firstOnes(WordCount(2)).shortenedTo(LetterCount(6))
        // Assert
        assertThat(shortenedWords).isEqualTo(Words(Word("BASKET"), Word("SHOES")))
    }

    @Test
    fun `the words separated by hyphens should be the beginning of a SKU`() {
        // Act
        val prefix = Words(Word("CLO"), Word("ORGANIC")).hyphenated()
        // Assert
        assertThat(prefix).isEqualTo(SkuPrefix("CLO-ORGANIC"))
    }

    @Test
    fun `an empty text should have no word`() {
        // Act
        val words = Words.of("")
        // Assert
        assertThat(words).isEqualTo(Words())
    }
}
