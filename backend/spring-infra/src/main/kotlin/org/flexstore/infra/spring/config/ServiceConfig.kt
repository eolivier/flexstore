package org.flexstore.infra.spring.config

import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.discount.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The domain services, each one a bean that receives its use cases or groups of use cases (see UseCaseConfig)
@Configuration
open class ServiceConfig {

    @Bean
    open fun paymentService(paymentUseCases: PaymentUseCases,
                            orderUseCases: OrderUseCases,
                            issueInvoiceUseCase: IssueInvoiceUseCase) =
        PaymentService(paymentUseCases, orderUseCases, issueInvoiceUseCase)

    @Bean
    open fun invoiceService(invoiceUseCases: InvoiceUseCases,
                            readAdministeredOrderUseCase: ReadAdministeredOrderUseCase) =
        InvoiceService(invoiceUseCases, readAdministeredOrderUseCase)

    @Bean
    open fun shopSettingsAdministrationService(readShopSettingsUseCase: ReadShopSettingsUseCase,
                                        updateShopSettingsUseCase: UpdateShopSettingsUseCase):
        ShopSettingsAdministrationService {
        return ShopSettingsAdministrationService(readShopSettingsUseCase, updateShopSettingsUseCase)
    }

    @Bean
    open fun orderCheckoutService(orderPlacementUseCases: OrderPlacementUseCases,
                                  checkoutQueryUseCases: CheckoutQueryUseCases,
                                  checkoutDiscountUseCases: CheckoutDiscountUseCases) =
        OrderCheckoutService(orderPlacementUseCases, checkoutQueryUseCases, checkoutDiscountUseCases)

    @Bean
    open fun orderAdministrationService(listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
                                        readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
                                        advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
                                        refundOrderUseCase: RefundOrderUseCase,
                                        sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase) =
        OrderAdministrationService(
            listAdministeredOrdersUseCase,
            readAdministeredOrderUseCase,
            advanceOrderStatusUseCase,
            refundOrderUseCase,
            sendOrderStatusEmailUseCase
        )

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
    open fun passwordResetService(requestPasswordResetUseCase: RequestPasswordResetUseCase,
                                  resetPasswordUseCase: ResetPasswordUseCase) =
        PasswordResetService(requestPasswordResetUseCase, resetPasswordUseCase)

    @Bean
    open fun unpaidOrderService(readShopSettingsUseCase: ReadShopSettingsUseCase,
                                listAllOrdersUseCase: ListAllOrdersUseCase,
                                cancelUnpaidOrderUseCase: CancelUnpaidOrderUseCase) =
        UnpaidOrderService(readShopSettingsUseCase, listAllOrdersUseCase, cancelUnpaidOrderUseCase)

    @Bean
    open fun addressBookService(readAccountUseCase: ReadAccountUseCase, addressUseCases: AddressUseCases) =
        AddressBookService(readAccountUseCase, addressUseCases)

    @Bean
    open fun userService(userRepository: UserRepository): UserService {
        return UserService(userRepository)
    }
}
