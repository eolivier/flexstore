package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads the discount codes given by the administrators, newest first
class ListDiscountCodesUseCase(
    private val discountCodeRepository: DiscountCodeRepository,
    private val userRepository: UserRepository
) : QueryUseCase<DiscountCodesReading, DiscountCodes> {

    override fun getPreConditions(): PreConditions<DiscountCodesReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<DiscountCodesReading, DiscountCodes> { discountCodeRepository.findAll() }

    override fun getPostConditions(): PostConditions<DiscountCodes> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<DiscountCodesReading> { reading ->
        if (userRepository.isNotAdministrator(reading.reader)) {
            throw DiscountCodeAdministrationForbiddenException(
                ErrorMessage("Only an administrator can read the discount codes.")
            )
        }
    }
}
