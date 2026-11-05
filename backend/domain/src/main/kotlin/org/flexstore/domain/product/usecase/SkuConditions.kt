package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.product.model.InvalidSkuException
import org.flexstore.domain.product.model.Sku
import org.flexstore.domain.product.model.SkuAlreadyUsedException
import org.ucop.domain.ErrorMessage

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
