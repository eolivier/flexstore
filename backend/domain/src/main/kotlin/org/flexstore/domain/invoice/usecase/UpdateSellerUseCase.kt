package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator gives a new version of the seller written on the invoices: its name and address are given,
// its SIRET and VAT number are well formed. It becomes the current one
class UpdateSellerUseCase(private val sellerRepository: SellerRepository) : UseCase<Seller> {

    override fun getPreConditions(): PreConditions<Seller> =
        PreConditions(contactIsCompleteCondition(), identifiersAreValidCondition())

    override fun getNominalScenario(): NominalScenario<Seller> =
        NominalScenario(Step { seller -> sellerRepository.save(seller) })

    override fun getPostConditions(): PostConditions<Seller> = PostConditions(sellerIsSavedCondition())

    private fun contactIsCompleteCondition() = PreCondition<Seller> { seller ->
        if (seller.details.contact.isIncomplete()) {
            throw InvalidSellerException(ErrorMessage("The seller needs a name and an address."))
        }
    }

    private fun identifiersAreValidCondition() = PreCondition<Seller> { seller ->
        if (seller.details.identifiers.areInvalid()) {
            val rule = "a SIRET of 14 digits, a VAT number of its country (FR) then 2 to 13 digits or letters"
            throw InvalidSellerException(ErrorMessage("The seller needs $rule."))
        }
    }

    private fun sellerIsSavedCondition() = PostCondition<Seller> { seller ->
        val currentSeller = sellerRepository.current()
        if (currentSeller != seller) {
            throw SellerUpdateFailed(ErrorMessage("The seller was not saved."))
        }
    }
}
