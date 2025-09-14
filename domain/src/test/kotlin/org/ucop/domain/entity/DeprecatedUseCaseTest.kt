package org.ucop.domain.entity

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.math.BigDecimal

class DeprecatedUseCaseTest {

    @Test
    fun `should add to basket`() {
        // given
        val itemRepository = spy(InMemoryItemRepository())
        itemRepository.clear()
        val myCart = Cart(itemRepository)
        val cartIsEmptyCondition = PreCondition<Cart> { basket -> assert(basket.getItems().isEmpty()) }
        val item1 = createItem1()
        val addOneItemToCartStep = Step<Cart>(
            run = {
                basket -> run {

                basket.addOrReplaceItem(item1)
            }
        })
        val cartHasOneItemCondition = PostCondition<Cart> { basket -> assert(basket.getItems().size == 1) }

        val addOneItemToBasketScenario = NominalScenario(listOf(addOneItemToCartStep))

        val addOneItemToBasketDeprecatedUseCase = DeprecatedUseCase(
            preConditions = listOf(cartIsEmptyCondition),
            nominalScenario = addOneItemToBasketScenario,
            postConditions = listOf(cartHasOneItemCondition)
        )
        // when
        addOneItemToBasketDeprecatedUseCase.run(myCart)
        // then
        verify(itemRepository).add(item1)
    }

    fun createItem1(): OneItem {
        val itemId = ItemId(Identity("item1"))
        val product = Product(ProductId(Identity("product1")), Name("product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
        return OneItem(itemId, product, Quantity(10))
    }
}