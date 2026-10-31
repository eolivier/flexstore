package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*

// The use cases of the discount codes, for the administrators
class DiscountCodeUseCases(
    private val createDiscountCodeUseCase: CreateDiscountCodeUseCase,
    private val listDiscountCodesUseCase: ListDiscountCodesUseCase,
    private val deleteDiscountCodeUseCase: DeleteDiscountCodeUseCase
) {
    fun create(discountCode: DiscountCode) = createDiscountCodeUseCase.unfold(discountCode)

    fun getAll() = listDiscountCodesUseCase.unfold(Unit)

    fun delete(code: PromoCode) = deleteDiscountCodeUseCase.unfold(code)
}

// The use cases of the discount codes, for the checkout: a code entered is read usable, then used by the order
data class CheckoutDiscountUseCases(
    val readUsableDiscountCodeUseCase: ReadUsableDiscountCodeUseCase,
    val useDiscountCodeUseCase: UseDiscountCodeUseCase
)
