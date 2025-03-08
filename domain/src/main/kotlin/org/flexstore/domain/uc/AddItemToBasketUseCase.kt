package org.flexstore.domain.uc

import org.flexstore.domain.*

private const val ONE_ITEM = 1

class AddItemToBasketUseCase(private val currentBasket: Basket) {

    fun execute(newItem: Item, quantity: Quantity) {
        // Preconditions
        val basketIsEmptyCondition = PreCondition<Basket> { basket -> assert(basket.getItems().isEmpty()) }
        // Steps
        val addOneItemToBasketStep = Step<Basket> { basket -> basket.changeQuantity(newItem.itemId, quantity) }
        // Postconditions
        val basketHasOneItemCondition = PostCondition<Basket> { basket -> assert(basket.getItems().size == ONE_ITEM) }
        // Nominal scenario
        val addOneItemToBasketScenario = NominalScenario(listOf(addOneItemToBasketStep))
        // Use case
        val addOneItemToBasketUseCase = UseCase(
            preConditions = listOf(basketIsEmptyCondition),
            nominalScenario = addOneItemToBasketScenario,
            postConditions = listOf(basketHasOneItemCondition)
        )
        // Run use case
        addOneItemToBasketUseCase.run(currentBasket)
    }
}