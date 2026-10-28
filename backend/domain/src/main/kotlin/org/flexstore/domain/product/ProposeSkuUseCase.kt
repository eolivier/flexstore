package org.flexstore.domain.product

import org.ucop.domain.UseCaseLog
import java.text.Normalizer

// What a SKU is proposed from: the category of the product and its name in the default language, maybe not
// entered yet
data class SkuBasis(val category: Category, val name: String)

// Proposes a SKU to an administrator for a product: the first three letters of its category, the first two words
// of its name (eight characters at most each, without accents), then the first number from 0001 that no product
// of the catalog has (CLO-ORGANIC-COTTON-0001)
class ProposeSkuUseCase(private val productRepository: ProductRepository) {

    fun proposeFor(skuBasis: SkuBasis): Sku = UseCaseLog.execution("ProposeSkuUseCase.proposeFor") {
        val prefix = prefixOf(skuBasis)
        generateSequence(1) { it + 1 }
            .map { number -> Sku("$prefix-${number.toString().padStart(NUMBER_LENGTH, '0')}") }
            .first { sku -> productRepository.skuIsFree(sku) }
    }

    private fun prefixOf(skuBasis: SkuBasis): String {
        val categoryLetters = skuBasis.category.name.take(CATEGORY_LETTERS)
        val nameWords = wordsOf(skuBasis.name).take(NAME_WORDS).map { it.take(WORD_LETTERS) }
        return (listOf(categoryLetters) + nameWords).joinToString("-")
    }

    // The words of the name in upper case, without accents nor other signs ("T-shirt bio" gives T, SHIRT, BIO)
    private fun wordsOf(name: String): List<String> {
        val withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
        return withoutAccents.uppercase().split(Regex("[^A-Z0-9]+")).filter { it.isNotEmpty() }
    }
}

private const val CATEGORY_LETTERS = 3
private const val NAME_WORDS = 2
private const val WORD_LETTERS = 8
private const val NUMBER_LENGTH = 4
