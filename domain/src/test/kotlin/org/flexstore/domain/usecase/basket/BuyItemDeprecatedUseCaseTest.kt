package org.flexstore.domain.usecase.basket

import org.flexstore.domain.valueobject.*
import org.ucop.domain.entity.Name
import java.math.BigDecimal
import kotlin.test.Test

class BuyItemDeprecatedUseCaseTest {

    @Test
    fun `preConditions should throw exception for invalid item`() {
        // given
        val invalidItem = createInvalidItem()
        //val buyItemUc = BuyItemUseCase(invalidItem)
        // when / then
            //assertThatThrownBy { buyItemUc.preConditions() }
        //.isInstanceOf(PreConditionException::class.java)
    }

    @Test
    fun `nominal should process valid item`() {
        // given
        val validItem = createItem1()
        //val buyItemUc = BuyItemUseCase(validItem)
        // when
        // buyItemUc.nominal()
        // then
        // Add assertions based on expected state changes or interactions
    }

    @Test
    fun `alternative should handle alternative flow`() {
        // given
        val validItem = createItem1()
        //val buyItemUc = BuyItemUseCase(validItem)
        // when
        //buyItemUc.alternative()
        // then
        // Add assertions based on expected state changes or interactions
    }

    @Test
    fun `postConditions should verify post conditions`() {
        // given
        val validItem = createItem1()
        //val buyItemUc = BuyItemUseCase(validItem)
        // when
        //buyItemUc.postConditions()
        // then
        // Add assertions based on expected state changes or interactions
    }

    private fun createInvalidItem(): Item {
        return Item(TestData.itemId1, TestData.product1, TestData.quantity4)
    }

    private fun createItem1(): Item {
        return TestData.item1
    }

    object TestData {
        val itemId1 = ItemId(Identity("item1"))
        val itemId2 = ItemId(Identity("item2"))
        val product1 = Product(ProductId(Identity("product1")), Name("Product 1"), Price(Amount(BigDecimal("10.00")), Currency.EUR))
        val product2 = Product(ProductId(Identity("product2")), Name("Product 2"), Price(Amount(BigDecimal("20.00")), Currency.USD))
        val quantity1 = Quantity(5)
        val quantity2 = Quantity(10)
        val quantity3 = Quantity(0)
        val quantity4 = Quantity(-1)

        val item1 = Item(itemId1, product1, quantity1)
        val item2 = Item(itemId2, product2, quantity2)
        val item3 = Item(itemId1, product1, quantity3)
        val item4 = Item(itemId2, product2, quantity4)
    }
}