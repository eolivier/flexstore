package org.flexstore.domain

class BuyItemUseCase(private val item: Item) : UseCase<Item> {

    override fun preConditions() {
        if (item.quantity.isNegative()) {
            throw PreConditionException(NonEmptyString("Quantity must be positive"))
        }
    }

    override fun nominal() {
        item.decrease()
    }

    override fun alternative() {
        TODO("Not yet implemented")
    }

    override fun postConditions() {
        if (item.quantity.isNotZero()) {
            throw PostConditionException(NonEmptyString("Quantity must be zero"))
        }
    }
}