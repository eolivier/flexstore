package org.flexstore.domain.stock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryStockRepository
import org.flexstore.domain.user.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.unknownUser
import org.flexstore.domain.user.administration.usersOfTheAdministration

class StockUseCasesTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val stockRepository = InMemoryStockRepository()
    private val changeStockUseCase =
        ChangeStockUseCase(stockRepository, productRepository, usersOfTheAdministration())
    private val stockUseCases =
        StockUseCases(ListStocksUseCase(stockRepository), ReadStockUseCase(stockRepository), changeStockUseCase)

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
        val stock = stockUseCases.change(testAdministrator.id, Stock(deskLamp.productId, StockQuantity(12)))
        // Assert
        assertThat(stock).isEqualTo(Stock(deskLamp.productId, StockQuantity(12)))
    }

    @Test
    fun `a stock should not be negative`() {
        // Arrange
        val negativeStock = Stock(deskLamp.productId, StockQuantity(-1))
        // Act
        val thrown = catchThrowable { stockUseCases.change(testAdministrator.id, negativeStock) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(NegativeStockException::class.java)
    }

    @Test
    fun `the stock of an unknown product should not be given`() {
        // Arrange
        val unknownProductStock = Stock(ProductId(Identity("unknown")), StockQuantity(3))
        // Act
        val thrown = catchThrowable { stockUseCases.change(testAdministrator.id, unknownProductStock) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
    }

    @Test
    fun `a customer should not change the stock of a product`() {
        // Arrange
        val stock = Stock(deskLamp.productId, StockQuantity(12))
        // Act
        val thrown = catchThrowable { stockUseCases.change(testCustomer.id, stock) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockChangeForbiddenException::class.java)
        assertThat(stockRepository.stockOf(deskLamp.productId)).isEqualTo(Stock.noneOf(deskLamp.productId))
    }

    @Test
    fun `an unknown user should not change the stock of a product`() {
        // Arrange
        val stock = Stock(deskLamp.productId, StockQuantity(12))
        // Act
        val thrown = catchThrowable { stockUseCases.change(unknownUser, stock) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockChangeForbiddenException::class.java)
    }
}
