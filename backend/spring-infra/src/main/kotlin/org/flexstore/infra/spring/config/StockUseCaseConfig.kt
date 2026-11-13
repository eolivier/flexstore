package org.flexstore.infra.spring.config

import org.flexstore.domain.product.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the stock sub-domain, declared step by step: use cases, then their group
@Configuration
open class StockUseCaseConfig {

    @Bean
    open fun readStockUseCase(stockRepository: StockRepository) = ReadStockUseCase(stockRepository)

    @Bean
    open fun listStocksUseCase(stockRepository: StockRepository) = ListStocksUseCase(stockRepository)

    @Bean
    open fun changeStockUseCase(stockRepository: StockRepository, productRepository: ProductRepository) =
        ChangeStockUseCase(stockRepository, productRepository)

    @Bean
    open fun listLowStocksUseCase(stockRepository: StockRepository) = ListLowStocksUseCase(stockRepository)

    @Bean
    open fun readStockAlertUseCase(stockRepository: StockRepository) = ReadStockAlertUseCase(stockRepository)

    @Bean
    open fun changeStockAlertUseCase(stockRepository: StockRepository, productRepository: ProductRepository) =
        ChangeStockAlertUseCase(stockRepository, productRepository)

    @Bean
    open fun stockUseCases(listStocksUseCase: ListStocksUseCase,
                           readStockUseCase: ReadStockUseCase,
                           changeStockUseCase: ChangeStockUseCase) =
        StockUseCases(listStocksUseCase, readStockUseCase, changeStockUseCase)

    @Bean
    open fun stockAlertUseCases(listLowStocksUseCase: ListLowStocksUseCase,
                                readStockAlertUseCase: ReadStockAlertUseCase,
                                changeStockAlertUseCase: ChangeStockAlertUseCase) =
        StockAlertUseCases(listLowStocksUseCase, readStockAlertUseCase, changeStockAlertUseCase)
}
