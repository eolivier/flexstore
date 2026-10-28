package org.flexstore.domain.product

import org.ucop.domain.UseCaseLog

// Proposes a SKU to an administrator for a product: the first SKU proposed for it (see Sku.proposalsFor) that no
// product of the catalog has
class ProposeSkuUseCase(private val productRepository: ProductRepository) {

    fun proposeFor(skuBasis: SkuBasis): Sku = UseCaseLog.execution("ProposeSkuUseCase.proposeFor") {
        Sku.proposalsFor(skuBasis).first { sku -> productRepository.skuIsFree(sku) }
    }
}
