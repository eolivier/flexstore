package org.flexstore.domain.loyalty.usecase

import org.ucop.domain.entity.Actor

// The use cases of the loyalty programme
class LoyaltyUseCases(private val readLoyaltyPointsUseCase: ReadLoyaltyPointsUseCase) {

    fun getStatement(customer: Actor) = readLoyaltyPointsUseCase.unfold(customer)
}
