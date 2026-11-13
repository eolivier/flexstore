package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// Proposes a SKU to an administrator for a product: the first SKU proposed for it (see SkuBasis.proposals) that
// no product of the catalog has. The answer is checked after: a well-formed SKU, free in the catalog
class ProposeSkuUseCase(
    private val productRepository: ProductRepository
) : QueryUseCase<SkuProposalRequest, Sku> {

    override fun getPreConditions(): PreConditions<SkuProposalRequest> =
        PreConditions(requesterIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<SkuProposalRequest, Sku> { (_, skuBasis) ->
        skuBasis.proposals().firstAccepted { sku -> productRepository.skuIsFree(sku) }
    }

    override fun getPostConditions(): PostConditions<Sku> =
        PostConditions(proposalIsWellFormedCondition(), proposalIsFreeCondition())

    // The requester, resolved before the use case runs, must be an Admin
    private fun requesterIsAdministratorCondition() = PreCondition<SkuProposalRequest> { request ->
        if (request.requester.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can ask for a SKU.")
            throw ProductAdministrationForbiddenException(message)
        }
    }

    private fun proposalIsWellFormedCondition() = PostCondition<Sku> { proposedSku ->
        if (proposedSku.isMalformed()) {
            throw SkuProposalFailed(ErrorMessage("The SKU proposed, ${proposedSku.value}, is malformed."))
        }
    }

    private fun proposalIsFreeCondition() = PostCondition<Sku> { proposedSku ->
        if (productRepository.skuIsNotFree(proposedSku)) {
            throw SkuProposalFailed(ErrorMessage("The SKU proposed, ${proposedSku.value}, is already used."))
        }
    }
}
