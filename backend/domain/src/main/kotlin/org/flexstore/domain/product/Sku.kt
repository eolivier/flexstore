package org.flexstore.domain.product

import java.text.Normalizer

// Stock Keeping Unit: the reference the shop gives to a product to identify it in its stock and its orders,
// unique in the catalog (TSHIRT-ORGANIC-M). Upper-case letters and digits, in groups separated by single hyphens
data class Sku(val value: String) {

    fun isWellFormed() = value.length <= MAX_LENGTH && WELL_FORMED.matches(value)

    fun isMalformed() = !isWellFormed()

    companion object {
        const val MAX_LENGTH = 32

        private val WELL_FORMED = Regex("[A-Z0-9]+(-[A-Z0-9]+)*")

        // As typed by an administrator: the spaces around are dropped, the letters put in upper case
        fun from(typedSku: String) = Sku(typedSku.trim().uppercase())
    }
}

// What a SKU is proposed from: the category of the product and the words of its name in the default language,
// maybe none yet
data class SkuBasis(val category: Category, val nameWords: Words) {

    // The beginning of the SKUs proposed: the first three letters of the category, then the first two words of
    // the name, eight characters at most each (CLO-ORGANIC-COTTON)
    fun prefix(): SkuPrefix {
        val categoryWord = Word(category.name).shortenedTo(CATEGORY_LETTERS)
        val firstNameWords = nameWords.firstOnes(NAME_WORDS).shortenedTo(WORD_LETTERS)
        return (Words(categoryWord) + firstNameWords).hyphenated()
    }

    fun proposals() = SkuProposals(prefix())
}

// The SKUs proposed for a product, in order: its prefix numbered from 0001 (CLO-ORGANIC-COTTON-0001,
// CLO-ORGANIC-COTTON-0002...)
data class SkuProposals(private val prefix: SkuPrefix) {

    // The first SKU proposed that is accepted (free in the catalog, for instance)
    fun firstAccepted(accepts: (Sku) -> Boolean): Sku =
        generateSequence(SkuNumber.first()) { it.next() }.map { prefix.numbered(it) }.first(accepts)
}

// The beginning of the SKUs proposed for a product (CLO-ORGANIC-COTTON), followed by their number
data class SkuPrefix(val value: String) {

    // The SKU of this number, on four digits (CLO-ORGANIC-COTTON-0001)
    fun numbered(skuNumber: SkuNumber) = Sku("$value-${skuNumber.value.toString().padStart(NUMBER_LENGTH, '0')}")
}

// The number that ends a SKU proposed, from 1
data class SkuNumber(val value: Int) {

    fun next() = SkuNumber(value + 1)

    companion object {
        fun first() = SkuNumber(1)
    }
}

// A word in upper case, without accents nor other signs (SHIRT)
data class Word(val value: String) {

    fun shortenedTo(letterCount: LetterCount) = Word(value.take(letterCount.value))
}

// A number of letters of a word
data class LetterCount(val value: Int)

// A number of words
data class WordCount(val value: Int)

// Words, in the order they are written
data class Words(private val words: List<Word>) {

    constructor(vararg words: Word) : this(words.toList())

    fun firstOnes(wordCount: WordCount) = Words(words.take(wordCount.value))

    // Each word shortened to its first letters
    fun shortenedTo(letterCount: LetterCount) = Words(words.map { it.shortenedTo(letterCount) })

    operator fun plus(otherWords: Words) = Words(words + otherWords.words)

    // The words separated by hyphens, as the beginning of a SKU
    fun hyphenated() = SkuPrefix(words.joinToString("-") { it.value })

    companion object {
        // The words of a text, in upper case, without accents nor other signs ("T-shirt bio" gives T, SHIRT, BIO)
        fun of(text: String): Words {
            val withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            val letterGroups = withoutAccents.uppercase().split(Regex("[^A-Z0-9]+")).filter { it.isNotEmpty() }
            return Words(letterGroups.map { Word(it) })
        }
    }
}

private val CATEGORY_LETTERS = LetterCount(3)
private val NAME_WORDS = WordCount(2)
private val WORD_LETTERS = LetterCount(8)
private const val NUMBER_LENGTH = 4
