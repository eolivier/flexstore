package org.flexstore.domain.usecase.basket

import org.flexstore.domain.entity.*
import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.Quantity
import org.ucop.domain.entity.*

private const val ONE_ITEM = 1

private const val BASKET_IS_NOT_EMPTY = "Basket is not empty."

class AddItemToBasketUseCase(private val currentCart: Cart) {

    fun perform(newItem: Item, quantity: Quantity) {
        // Preconditions
        val cartIsEmptyCondition = PreCondition<Cart> {
            basket -> assert(basket.getItems().items.isEmpty()) { BASKET_IS_NOT_EMPTY }
        }
        // Steps
        val addOneItemToCartStep = Step<Cart> { basket -> basket.changeQuantity(newItem, quantity) }
        // Postconditions
        val cartHasOneItemCondition = PostCondition<Cart> {
            basket -> assert(basket.getItems().items.size == ONE_ITEM) { "Basket does not contain exactly one item. Found ${basket.getItems().items.size}" }
        }
        // Nominal scenario
        val addOneItemToBasketScenario = NominalScenario(listOf(addOneItemToCartStep))
        // Use case
        val addOneItemToBasketDeprecatedUseCase = DeprecatedUseCase(
            preConditions = listOf(cartIsEmptyCondition),
            nominalScenario = addOneItemToBasketScenario,
            postConditions = listOf(cartHasOneItemCondition)
        )
        // Run use case
        addOneItemToBasketDeprecatedUseCase.run(currentCart)
    }
}