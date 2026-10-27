package org.flexstore.infra.spring.adapter.persistence.jpa

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Quantity
import org.flexstore.domain.stock.*
import org.flexstore.infra.spring.rest.RefilledStocks
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.Callable
import java.util.concurrent.Executors

// The reservations of the database: the stock is checked and taken in one statement
@ExtendWith(RefilledStocks::class)
@ActiveProfiles("test")
@SpringBootTest
class StockReservationIT {

    @Autowired
    private lateinit var stockRepository: StockRepository

    // The Laptop of the catalog
    private val laptopId = ProductId(Identity("6"))

    private fun laptopStockOf(units: Int) = stockRepository.save(Stock(laptopId, StockQuantity(units)))

    private fun laptopUnits() = stockRepository.stockOf(laptopId).quantity

    @Test
    fun `a reservation covered by the stock should take its units out`() {
        // Arrange
        laptopStockOf(5)
        // Act
        val stockReservation = stockRepository.reserve(laptopId, Quantity(2))
        // Assert
        assertThat(stockReservation).isEqualTo(StockReservation.RESERVED)
        assertThat(laptopUnits()).isEqualTo(StockQuantity(3))
    }

    @Test
    fun `a reservation not covered by the stock should be refused and leave the stock as it is`() {
        // Arrange
        laptopStockOf(1)
        // Act
        val stockReservation = stockRepository.reserve(laptopId, Quantity(2))
        // Assert
        assertThat(stockReservation).isEqualTo(StockReservation.NOT_COVERED)
        assertThat(laptopUnits()).isEqualTo(StockQuantity(1))
    }

    @Test
    fun `a release should give the units back to the stock`() {
        // Arrange
        laptopStockOf(3)
        // Act
        stockRepository.release(laptopId, Quantity(2))
        // Assert
        assertThat(laptopUnits()).isEqualTo(StockQuantity(5))
    }

    @Test
    fun `reservations made at the same time should never take more units than the stock has`() {
        // Arrange
        laptopStockOf(5)
        val executor = Executors.newFixedThreadPool(20)
        val reservation = Callable { stockRepository.reserve(laptopId, Quantity(1)) }
        // Act
        val stockReservations = executor.invokeAll(List(20) { reservation }).map { it.get() }
        executor.shutdown()
        // Assert
        assertThat(stockReservations.count { it == StockReservation.RESERVED }).isEqualTo(5)
        assertThat(laptopUnits()).isEqualTo(StockQuantity(0))
    }
}
