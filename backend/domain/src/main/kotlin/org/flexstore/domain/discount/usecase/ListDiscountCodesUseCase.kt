package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the discount codes given by the administrators, newest first
class ListDiscountCodesUseCase(
    private val discountCodeRepository: DiscountCodeRepository
) : QueryUseCase<DiscountCodesReading, DiscountCodes> {

    override fun getPreConditions(): PreConditions<DiscountCodesReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<DiscountCodesReading, DiscountCodes> { discountCodeRepository.findAll() }

    override fun getPostConditions(): PostConditions<DiscountCodes> = PostConditions.none()

    // The reader, resolved before the use case runs, must be an Admin
    private fun readerIsAdministratorCondition() = PreCondition<DiscountCodesReading> { reading ->
        if (reading.reader.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can read the discount codes.")
            throw DiscountCodeAdministrationForbiddenException(message)
        }
    }
}
