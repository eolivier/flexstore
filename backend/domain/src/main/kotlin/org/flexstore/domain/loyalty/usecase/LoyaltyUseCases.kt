package org.flexstore.domain.loyalty.usecase

import org.flexstore.domain.user.model.UserId

// The use cases of the loyalty programme
class LoyaltyUseCases(private val readLoyaltyPointsUseCase: ReadLoyaltyPointsUseCase) {

    fun getPoints(customer: UserId.ValidUserId) = readLoyaltyPointsUseCase.unfold(customer)
}
