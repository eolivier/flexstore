package org.flexstore.domain.order.usecase

// The use cases that read the orders, held together for the groups and services that read them
data class OrderQueryUseCases(
    val listCustomerOrdersUseCase: ListCustomerOrdersUseCase,
    val listAllOrdersUseCase: ListAllOrdersUseCase,
    val readAnyOrderUseCase: ReadAnyOrderUseCase,
    val readCustomerOrderUseCase: ReadCustomerOrderUseCase
)
