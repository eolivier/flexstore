package org.flexstore.domain.stock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryStockRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class StockUseCasesTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val stockRepository = InMemoryStockRepository()
    private val changeStockUseCase = ChangeStockUseCase(stockRepository, productRepository)
    private val stockUseCases = StockUseCases(ReadStockUseCase(stockRepository), changeStockUseCase)

    @Test
    fun `a product whose stock was never given should have none`() {
        // Act
        val stock = stockUseCases.getStock(deskLamp.productId)
        // Assert
        assertThat(stock.quantity).isEqualTo(StockQuantity.none())
    }

    @Test
    fun `an administrator should give the stock of a product`() {
        // Act
        val stock = stockUseCases.change(Stock(deskLamp.productId, StockQuantity(12)))
        // Assert
        assertThat(stock).isEqualTo(Stock(deskLamp.productId, StockQuantity(12)))
    }

    @Test
    fun `a stock should not be negative`() {
        // Arrange
        val negativeStock = Stock(deskLamp.productId, StockQuantity(-1))
        // Act
        val thrown = catchThrowable { stockUseCases.change(negativeStock) }
        // Assert
        assertThat(thrown).isInstanceOf(NegativeStockException::class.java)
    }

    @Test
    fun `the stock of an unknown product should not be given`() {
        // Arrange
        val unknownProductStock = Stock(ProductId(Identity("unknown")), StockQuantity(3))
        // Act
        val thrown = catchThrowable { stockUseCases.change(unknownProductStock) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
    }
}
