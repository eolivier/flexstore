package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage

// The SKU of a created or edited product: well formed, and not used by another product of the catalog

internal fun throwIfMalformed(sku: Sku) {
    if (sku.isMalformed()) {
        val rule = "upper-case letters and digits, in groups separated by hyphens, ${Sku.MAX_LENGTH} at most"
        throw InvalidSkuException(ErrorMessage("SKU ${sku.value} is invalid: $rule."))
    }
}

internal fun throwIfUsedByAnotherProduct(product: DefinedProduct, productRepository: ProductRepository) {
    if (productRepository.skuIsUsedByAnotherProduct(product)) {
        throw SkuAlreadyUsedException(ErrorMessage("SKU ${product.sku.value} is used by another product."))
    }
}
