package org.flexstore.domain.usecase

import org.flexstore.domain.*
import org.flexstore.domain.entity.*
import org.ucop.domain.entity.*

private const val ONE_ITEM = 1

private const val BASKET_IS_NOT_EMPTY = "Basket is not empty."

class AddItemToBasketUseCase(private val currentBasket: Basket) {

    fun perform(newItem: Item, quantity: Quantity) {
        // Preconditions
        val basketIsEmptyCondition = PreCondition<Basket> {
            basket -> assert(basket.getItems().isEmpty()) { BASKET_IS_NOT_EMPTY }
        }
        // Steps
        val addOneItemToBasketStep = Step<Basket> { basket -> basket.changeQuantity(newItem.itemId, quantity) }
        // Postconditions
        val basketHasOneItemCondition = PostCondition<Basket> {
            basket -> assert(basket.getItems().size == ONE_ITEM) { "Basket does not contain exactly one item. Found ${basket.getItems().size}" }
        }
        // Nominal scenario
        val addOneItemToBasketScenario = NominalScenario(listOf(addOneItemToBasketStep))
        // Use case
        val addOneItemToBasketDeprecatedUseCase = DeprecatedUseCase(
            preConditions = listOf(basketIsEmptyCondition),
            nominalScenario = addOneItemToBasketScenario,
            postConditions = listOf(basketHasOneItemCondition)
        )
        // Run use case
        addOneItemToBasketDeprecatedUseCase.run(currentBasket)
    }
}