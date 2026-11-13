package org.flexstore.domain.stock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryStockRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class StockAlertUseCasesTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val wallClock = DefinedProduct(ProductId(Identity("wall-clock")), Name("Wall clock"), price)
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp); save(wallClock) }
    private val stockRepository = InMemoryStockRepository()
    private val administrator = userOf("admin-1", Role.ADMIN)
    private val customer = userOf("customer-1", Role.USER)
    private val userRepository = InMemoryUserRepository().apply {
        save(administrator)
        save(customer)
    }
    private val changeStockAlertUseCase = ChangeStockAlertUseCase(stockRepository, productRepository)
    private val readStockAlertUseCase = ReadStockAlertUseCase(stockRepository)
    private val listLowStocksUseCase = ListLowStocksUseCase(stockRepository)
    private val stockAlertUseCases =
        StockAlertUseCases(listLowStocksUseCase, readStockAlertUseCase, changeStockAlertUseCase)
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val administratorActor = actorResolutionService.actorOf(administrator.id)
    private val customerActor = actorResolutionService.actorOf(customer.id)

    private fun userOf(id: String, role: Role) = DefinedUser(
        ValidUserId(id), FullName(FirstName("Jane"), LastName("Doe")), Email("$id@flexstore.fr"),
        HashedPassword("hashed"), role
    )

    @Test
    fun `a product whose threshold was never given should have the default one`() {
        // Act
        val stockAlert = stockAlertUseCases.alertOf(administratorActor, deskLamp.productId)
        // Assert
        assertThat(stockAlert.threshold).isEqualTo(AlertThreshold(5))
    }

    @Test
    fun `an administrator should give the alert threshold of a product`() {
        // Arrange
        val tenUnits = StockAlert(deskLamp.productId, AlertThreshold(10))
        // Act
        val stockAlert = stockAlertUseCases.change(administratorActor, tenUnits)
        // Assert
        assertThat(stockAlert).isEqualTo(tenUnits)
    }

    @Test
    fun `an alert threshold should not be negative`() {
        // Arrange
        val negativeAlert = StockAlert(deskLamp.productId, AlertThreshold(-1))
        // Act
        val thrown = catchThrowable { stockAlertUseCases.change(administratorActor, negativeAlert) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(NegativeAlertThresholdException::class.java)
    }

    @Test
    fun `the alert threshold of an unknown product should not be given`() {
        // Arrange
        val unknownAlert = StockAlert(ProductId(Identity("unknown")), AlertThreshold(3))
        // Act
        val thrown = catchThrowable { stockAlertUseCases.change(administratorActor, unknownAlert) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductNotFoundException::class.java)
    }

    @Test
    fun `the stocks at or under the threshold of their product should be low, from the lowest`() {
        // Arrange
        stockRepository.save(Stock(deskLamp.productId, StockQuantity(5)))
        stockRepository.save(Stock(wallClock.productId, StockQuantity(8)))
        stockAlertUseCases.change(administratorActor, StockAlert(wallClock.productId, AlertThreshold(10)))
        // Act
        val lowStocks = stockAlertUseCases.lowStocks(administratorActor)
        // Assert
        assertThat(lowStocks.asList()).containsExactly(
            LowStock(Stock(deskLamp.productId, StockQuantity(5)), AlertThreshold(5)),
            LowStock(Stock(wallClock.productId, StockQuantity(8)), AlertThreshold(10))
        )
    }

    @Test
    fun `a stock above the threshold of its product should not be low`() {
        // Arrange
        stockRepository.save(Stock(deskLamp.productId, StockQuantity(6)))
        // Act
        val lowStocks = stockAlertUseCases.lowStocks(administratorActor)
        // Assert
        assertThat(lowStocks.asList()).isEmpty()
    }

    @Test
    fun `a customer should not change the alert threshold of a product`() {
        // Arrange
        val stockAlert = StockAlert(deskLamp.productId, AlertThreshold(10))
        // Act
        val thrown = catchThrowable { stockAlertUseCases.change(customerActor, stockAlert) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockAlertForbiddenException::class.java)
        val defaultAlert = StockAlert.byDefaultOf(deskLamp.productId)
        assertThat(stockRepository.alertOf(deskLamp.productId)).isEqualTo(defaultAlert)
    }

    @Test
    fun `an unknown user should not change the alert threshold of a product`() {
        // Act
        val thrown = catchThrowable {
            val unknownUser = actorResolutionService.actorOf(ValidUserId("unknown"))
            stockAlertUseCases.change(unknownUser, StockAlert(deskLamp.productId, AlertThreshold(10)))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockAlertForbiddenException::class.java)
    }

    @Test
    fun `a customer should not read the alert threshold of a product`() {
        // Act
        val thrown = catchThrowable { stockAlertUseCases.alertOf(customerActor, deskLamp.productId) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockAlertForbiddenException::class.java)
    }

    @Test
    fun `a customer should not read the low stocks`() {
        // Act
        val thrown = catchThrowable { stockAlertUseCases.lowStocks(customerActor) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockAlertForbiddenException::class.java)
    }

    @Test
    fun `an unknown user should not read the low stocks`() {
        // Act
        val unknownUser = actorResolutionService.actorOf(ValidUserId("unknown"))
        val thrown = catchThrowable { stockAlertUseCases.lowStocks(unknownUser) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(StockAlertForbiddenException::class.java)
    }
}
