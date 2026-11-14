package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.legal.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the order sub-domain, declared step by step: use cases, then their group
@Configuration
open class OrderUseCaseConfig {

    @Bean
    open fun sendOrderConfirmationEmailUseCase(shopSettingsRepository: ShopSettingsRepository,
                                               userRepository: UserRepository,
                                               emailRepository: EmailRepository) =
        SendOrderConfirmationEmailUseCase(shopSettingsRepository, userRepository, emailRepository)

    @Bean
    open fun keepOrderAddressesUseCase(userRepository: UserRepository) =
        KeepOrderAddressesUseCase(userRepository)

    @Bean
    open fun placeOrderUseCase(
        orderRepository: OrderRepository,
        stockRepository: StockRepository,
        legalPageRepository: LegalPageRepository,
        userRepository: UserRepository
    ) = PlaceOrderUseCase(orderRepository, stockRepository, legalPageRepository, userRepository)

    @Bean
    open fun reserveOrderStockUseCase(orderRepository: OrderRepository, stockRepository: StockRepository) =
        ReserveOrderStockUseCase(orderRepository, stockRepository)

    @Bean
    open fun listCustomerOrdersUseCase(orderRepository: OrderRepository) =
        ListCustomerOrdersUseCase(orderRepository)

    @Bean
    open fun listAllOrdersUseCase(orderRepository: OrderRepository) = ListAllOrdersUseCase(orderRepository)

    @Bean
    open fun readAnyOrderUseCase(orderRepository: OrderRepository) = ReadAnyOrderUseCase(orderRepository)

    @Bean
    open fun readCustomerOrderUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ReadCustomerOrderUseCase(orderRepository, userRepository)

    @Bean
    open fun refundOrderUseCase(
        orderRepository: OrderRepository,
        stockRepository: StockRepository,
        paymentRepository: PaymentRepository
    ) = RefundOrderUseCase(orderRepository, stockRepository, paymentRepository)

    @Bean
    open fun sendOrderStatusEmailUseCase(userRepository: UserRepository, emailRepository: EmailRepository) =
        SendOrderStatusEmailUseCase(userRepository, emailRepository)

    @Bean
    open fun listAdministeredOrdersUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ListAdministeredOrdersUseCase(orderRepository, userRepository)

    @Bean
    open fun readAdministeredOrderUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ReadAdministeredOrderUseCase(orderRepository, userRepository)

    @Bean
    open fun advanceOrderStatusUseCase(orderRepository: OrderRepository) =
        AdvanceOrderStatusUseCase(orderRepository)

    @Bean
    open fun correctOrderTrackingUseCase(orderRepository: OrderRepository) =
        CorrectOrderTrackingUseCase(orderRepository)

    @Bean
    open fun cancelUnpaidOrderUseCase(orderRepository: OrderRepository,
                                      stockRepository: StockRepository,
                                      paymentRepository: PaymentRepository) =
        CancelUnpaidOrderUseCase(orderRepository, stockRepository, paymentRepository)

    @Bean
    open fun proposeShippingOptionsUseCase(
        cartRepository: CartRepository,
        shippingMethodRepository: ShippingMethodRepository,
        shopSettingsRepository: ShopSettingsRepository,
        userRepository: UserRepository
    ) = ProposeShippingOptionsUseCase(cartRepository, shippingMethodRepository, shopSettingsRepository, userRepository)

    @Bean
    open fun listShippingMethodsUseCase(shippingMethodRepository: ShippingMethodRepository) =
        ListShippingMethodsUseCase(shippingMethodRepository)

    @Bean
    open fun createShippingMethodUseCase(shippingMethodRepository: ShippingMethodRepository) =
        CreateShippingMethodUseCase(shippingMethodRepository)

    @Bean
    open fun updateShippingMethodUseCase(shippingMethodRepository: ShippingMethodRepository) =
        UpdateShippingMethodUseCase(shippingMethodRepository)

    @Bean
    open fun readEnabledShippingMethodUseCase(shippingMethodRepository: ShippingMethodRepository,
                                              userRepository: UserRepository) =
        ReadEnabledShippingMethodUseCase(shippingMethodRepository, userRepository)

    @Bean
    open fun shippingMethodUseCases(listShippingMethodsUseCase: ListShippingMethodsUseCase,
                                    createShippingMethodUseCase: CreateShippingMethodUseCase,
                                    updateShippingMethodUseCase: UpdateShippingMethodUseCase,
                                    readEnabledShippingMethodUseCase: ReadEnabledShippingMethodUseCase) =
        ShippingMethodUseCases(
            listShippingMethodsUseCase,
            createShippingMethodUseCase,
            updateShippingMethodUseCase,
            readEnabledShippingMethodUseCase
        )

    @Bean
    open fun orderPlacementUseCases(keepOrderAddressesUseCase: KeepOrderAddressesUseCase,
                                    placeOrderUseCase: PlaceOrderUseCase,
                                    reserveOrderStockUseCase: ReserveOrderStockUseCase) =
        OrderPlacementUseCases(keepOrderAddressesUseCase, placeOrderUseCase, reserveOrderStockUseCase)

    @Bean
    open fun checkoutQueryUseCases(readCartUseCase: ReadCartUseCase,
                                   readShopSettingsUseCase: ReadShopSettingsUseCase,
                                   readCustomerOrderUseCase: ReadCustomerOrderUseCase) =
        CheckoutQueryUseCases(readCartUseCase, readShopSettingsUseCase, readCustomerOrderUseCase)

    @Bean
    open fun orderQueryUseCases(listCustomerOrdersUseCase: ListCustomerOrdersUseCase,
                                listAllOrdersUseCase: ListAllOrdersUseCase,
                                readAnyOrderUseCase: ReadAnyOrderUseCase,
                                readCustomerOrderUseCase: ReadCustomerOrderUseCase) =
        OrderQueryUseCases(
            listCustomerOrdersUseCase,
            listAllOrdersUseCase,
            readAnyOrderUseCase,
            readCustomerOrderUseCase
        )

    @Bean
    open fun orderUseCases(orderQueryUseCases: OrderQueryUseCases,
                           sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase,
                           readShopSettingsUseCase: ReadShopSettingsUseCase,
                           proposeShippingOptionsUseCase: ProposeShippingOptionsUseCase) =
        OrderUseCases(
            orderQueryUseCases,
            sendOrderConfirmationEmailUseCase,
            readShopSettingsUseCase,
            proposeShippingOptionsUseCase
        )

    @Bean
    open fun refundedOrderUseCases(readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
                                   sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase) =
        RefundedOrderUseCases(readAdministeredOrderUseCase, sendOrderStatusEmailUseCase)

    @Bean
    open fun unpaidOrderUseCases(listAllOrdersUseCase: ListAllOrdersUseCase,
                                 cancelUnpaidOrderUseCase: CancelUnpaidOrderUseCase) =
        UnpaidOrderUseCases(listAllOrdersUseCase, cancelUnpaidOrderUseCase)
}
