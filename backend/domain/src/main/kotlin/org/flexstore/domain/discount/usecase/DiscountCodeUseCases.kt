package org.flexstore.domain.discount.usecase

import org.flexstore.domain.discount.*
import org.flexstore.domain.user.model.UserId

// The use cases of the discount codes, for the administrators
class DiscountCodeUseCases(
    private val createDiscountCodeUseCase: CreateDiscountCodeUseCase,
    private val listDiscountCodesUseCase: ListDiscountCodesUseCase,
    private val deleteDiscountCodeUseCase: DeleteDiscountCodeUseCase
) {
    fun create(creator: UserId, discountCode: DiscountCode) =
        createDiscountCodeUseCase.unfold(DiscountCodeCreation(creator, discountCode))

    fun getAll(reader: UserId) = listDiscountCodesUseCase.unfold(DiscountCodesReading(reader))

    fun delete(editor: UserId, code: PromoCode) =
        deleteDiscountCodeUseCase.unfold(DiscountCodeDeletion(editor, code))
}

// The use cases of the discount codes, for the checkout: a code entered is read usable, then used by the order
data class CheckoutDiscountUseCases(
    val readUsableDiscountCodeUseCase: ReadUsableDiscountCodeUseCase,
    val useDiscountCodeUseCase: UseDiscountCodeUseCase
)
