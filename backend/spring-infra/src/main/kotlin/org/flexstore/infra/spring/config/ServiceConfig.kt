package org.flexstore.infra.spring.config

import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The domain services and the facades, each one a bean that receives its use cases (see UseCaseConfig)
@Configuration
open class ServiceConfig {

    @Bean
    open fun productUseCaseFacade(createProductUseCase: CreateProductUseCase,
                                 readProductUseCase: ReadProductUseCase,
                                 updateProductUseCase: UpdateProductUseCase): ProductUseCaseFacade {
        return ProductUseCaseFacade(createProductUseCase, readProductUseCase, updateProductUseCase)
    }

    @Bean
    open fun cartUseCaseFacade(addItemToCartUseCase: AddItemToCartUseCase,
                               changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                               readCartUseCase: ReadCartUseCase,
                               mergeCartsUseCase: MergeCartsUseCase): CartUseCaseFacade {
        return CartUseCaseFacade(
            addItemToCartUseCase, changeCartItemQuantityUseCase, readCartUseCase, mergeCartsUseCase
        )
    }

    @Bean
    open fun orderUseCaseFacade(placeOrderUseCase: PlaceOrderUseCase,
                                readOrdersUseCase: ReadOrdersUseCase): OrderUseCaseFacade {
        return OrderUseCaseFacade(placeOrderUseCase, readOrdersUseCase)
    }

    @Bean
    open fun paymentService(paymentUseCases: PaymentUseCases, paidOrderUseCases: PaidOrderUseCases) =
        PaymentService(paymentUseCases, paidOrderUseCases)

    @Bean
    open fun shopSettingsAdministrationService(readShopSettingsUseCase: ReadShopSettingsUseCase,
                                        updateShopSettingsUseCase: UpdateShopSettingsUseCase):
        ShopSettingsAdministrationService {
        return ShopSettingsAdministrationService(readShopSettingsUseCase, updateShopSettingsUseCase)
    }

    @Bean
    open fun orderAdministrationService(readOrdersUseCase: ReadOrdersUseCase,
                                        advanceOrderStatusUseCase: AdvanceOrderStatusUseCase):
        OrderAdministrationService {
        return OrderAdministrationService(readOrdersUseCase, advanceOrderStatusUseCase)
    }

    @Bean
    open fun customerRegistrationService(registerCustomerUseCase: RegisterCustomerUseCase,
                                         sendVerificationEmailUseCase: SendVerificationEmailUseCase,
                                         verifyEmailUseCase: VerifyEmailUseCase,
                                         readAccountUseCase: ReadAccountUseCase): CustomerRegistrationService {
        return CustomerRegistrationService(
            registerCustomerUseCase, sendVerificationEmailUseCase, verifyEmailUseCase, readAccountUseCase
        )
    }

    @Bean
    open fun addressBookService(readAccountUseCase: ReadAccountUseCase, addressUseCases: AddressUseCases) =
        AddressBookService(readAccountUseCase, addressUseCases)

    @Bean
    open fun userService(userRepository: UserRepository): UserService {
        return UserService(userRepository)
    }
}
