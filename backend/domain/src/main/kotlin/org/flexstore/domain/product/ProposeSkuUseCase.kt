package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// Proposes a SKU to an administrator for a product: the first SKU proposed for it (see Sku.proposalsFor) that no
// product of the catalog has. The answer is checked after: a well-formed SKU, free in the catalog
class ProposeSkuUseCase(private val productRepository: ProductRepository) : QueryUseCase<SkuBasis, Sku> {

    override fun getPreConditions(): PreConditions<SkuBasis> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<SkuBasis, Sku> { skuBasis ->
        Sku.proposalsFor(skuBasis).first { sku -> productRepository.skuIsFree(sku) }
    }

    override fun getPostConditions(): PostConditions<Sku> =
        PostConditions(proposalIsWellFormedCondition(), proposalIsFreeCondition())

    private fun proposalIsWellFormedCondition() = PostCondition<Sku> { proposedSku ->
        if (proposedSku.isMalformed()) {
            throw SkuProposalFailed(ErrorMessage("The SKU proposed, ${proposedSku.value}, is malformed."))
        }
    }

    private fun proposalIsFreeCondition() = PostCondition<Sku> { proposedSku ->
        if (!productRepository.skuIsFree(proposedSku)) {
            throw SkuProposalFailed(ErrorMessage("The SKU proposed, ${proposedSku.value}, is already used."))
        }
    }
}
