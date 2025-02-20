package org.flexstore.domain

class BuyItemUseCase(private val item: Item) : UseCase<Item> {

    override fun preConditions() {
        if (item.quantity.isNegative()) {
            throw PreConditionException("Quantity must be positive")
        }
    }

    override fun nominal() {
        item.decrease()
    }

    override fun alternative() {
        TODO("Not yet implemented")
    }

    override fun postConditions() {
        TODO("Not yet implemented")
    }
}