package org.ucop.domain.entity

import org.flexstore.domain.*
import org.flexstore.domain.entity.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DeprecatedUseCaseTest {

    @Test
    fun `should return hello from domain`() {
        // given
        /*val preCondition1 = Precondition({ println("the first condition must be true") })
        val preCondition2 = Precondition({ println("the second condition must be false")})
        val preConditions = listOf(preCondition1, preCondition2)
        val nominalScenario = NominalScenario(listOf(
            Step(NonEmptyString("step1")),
            Step(NonEmptyString("step2")),
        ))
        val postCondition1 = Postcondition(NonEmptyString("the variable x must be updated"))
        val postCondition2 = Postcondition(NonEmptyString("the variable y must be updated"))
        val postConditions = listOf(postCondition1, postCondition2)

        val newUseCase  = NewUseCase(preConditions, nominalScenario, postConditions)
        // when
        newUseCase.execute();
        // then
        //assertThat(message).isEqualTo("Hello from Domain!")*/
    }

    @Test
    fun `should add to basket`() {
        val myBasket = Basket()
        val basketIsEmptyCondition = PreCondition<Basket> { basket -> assert(basket.getItems().isEmpty()) }
        val addOneItemToBasketStep = Step<Basket>(
            run = {
                basket -> run {
                val itemId = ItemId(Identity("item1"))
                val product = Product(ProductId(Identity("product1")), Name("product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
                val newItem = Item(itemId, product, Quantity(10))
                basket.addOrReplaceItem(newItem)
            }
        })
        val basketHasOneItemCondition = PostCondition<Basket> { basket -> assert(basket.getItems().size == 1) }

        val addOneItemToBasketScenario = NominalScenario(listOf(addOneItemToBasketStep))

        val addOneItemToBasketDeprecatedUseCase = DeprecatedUseCase(
            preConditions = listOf(basketIsEmptyCondition),
            nominalScenario = addOneItemToBasketScenario,
            postConditions = listOf(basketHasOneItemCondition)
        )

        addOneItemToBasketDeprecatedUseCase.run(myBasket)
    }
}