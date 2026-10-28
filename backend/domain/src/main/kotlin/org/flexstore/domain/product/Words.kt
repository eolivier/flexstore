package org.flexstore.domain.product

import java.text.Normalizer

// A word in upper case, without accents nor other signs (SHIRT)
data class Word(val value: String) {

    fun shortenedTo(letterCount: LetterCount) = Word(value.take(letterCount.value))
}

// Words written one after the other, separated by hyphens (CLO-ORGANIC-COTTON)
data class HyphenatedWords(val value: String)

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

    // The words separated by hyphens (CLO-ORGANIC-COTTON)
    fun hyphenated() = HyphenatedWords(words.joinToString("-") { it.value })

    companion object {
        // The words of a text, in upper case, without accents nor other signs ("T-shirt bio" gives T, SHIRT, BIO)
        fun of(text: String): Words {
            val withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            val letterGroups = withoutAccents.uppercase().split(Regex("[^A-Z0-9]+")).filter { it.isNotEmpty() }
            return Words(letterGroups.map { Word(it) })
        }
    }
}
