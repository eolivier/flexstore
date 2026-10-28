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

        // The SKUs proposed for a product, in order: the first three letters of its category, the first two
        // words of its name (eight characters at most each, without accents), then a number from 0001
        // (CLO-ORGANIC-COTTON-0001, CLO-ORGANIC-COTTON-0002...)
        fun proposalsFor(skuBasis: SkuBasis): Sequence<Sku> {
            val prefix = prefixOf(skuBasis)
            return generateSequence(1) { it + 1 }
                .map { number -> Sku("$prefix-${number.toString().padStart(NUMBER_LENGTH, '0')}") }
        }

        private fun prefixOf(skuBasis: SkuBasis): String {
            val categoryLetters = skuBasis.category.name.take(CATEGORY_LETTERS)
            val nameWords = wordsOf(skuBasis.name).take(NAME_WORDS).map { it.take(WORD_LETTERS) }
            return (listOf(categoryLetters) + nameWords).joinToString("-")
        }

        // The words of the name in upper case, without accents nor other signs ("T-shirt bio" gives T, SHIRT,
        // BIO)
        private fun wordsOf(name: String): List<String> {
            val withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            return withoutAccents.uppercase().split(Regex("[^A-Z0-9]+")).filter { it.isNotEmpty() }
        }
    }
}

// What a SKU is proposed from: the category of the product and its name in the default language, maybe not
// entered yet
data class SkuBasis(val category: Category, val name: String)

private const val CATEGORY_LETTERS = 3
private const val NAME_WORDS = 2
private const val WORD_LETTERS = 8
private const val NUMBER_LENGTH = 4
