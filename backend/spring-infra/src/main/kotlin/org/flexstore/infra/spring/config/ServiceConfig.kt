package org.flexstore.infra.spring.config

import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.discount.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.seller.SellerSetupService
import org.flexstore.domain.seller.usecase.SellerUseCases
import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.twofactor.TwoFactorAuthenticationService
import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.usecase.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.user.service.*
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
    open fun shopSettingsAdministrationService(
        readAdministeredShopSettingsUseCase: ReadAdministeredShopSettingsUseCase,
        updateShopSettingsUseCase: UpdateShopSettingsUseCase
    ) = ShopSettingsAdministrationService(readAdministeredShopSettingsUseCase, updateShopSettingsUseCase)

    @Bean
    open fun sellerSetupService(
        sellerUseCases: SellerUseCases,
        shopSettingsAdministrationService: ShopSettingsAdministrationService
    ) = SellerSetupService(sellerUseCases, shopSettingsAdministrationService)

    @Bean
    open fun orderCheckoutService(orderPlacementUseCases: OrderPlacementUseCases,
                                  checkoutQueryUseCases: CheckoutQueryUseCases,
                                  checkoutDiscountUseCases: CheckoutDiscountUseCases) =
        OrderCheckoutService(orderPlacementUseCases, checkoutQueryUseCases, checkoutDiscountUseCases)

    @Bean
    open fun orderAdministrationService(listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
                                        readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
                                        advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
                                        correctOrderTrackingUseCase: CorrectOrderTrackingUseCase,
                                        sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase) =
        OrderAdministrationService(
            listAdministeredOrdersUseCase,
            readAdministeredOrderUseCase,
            advanceOrderStatusUseCase,
            correctOrderTrackingUseCase,
            sendOrderStatusEmailUseCase
        )

    @Bean
    open fun orderRefundService(refundOrderUseCase: RefundOrderUseCase,
                                issueCreditNoteUseCase: IssueCreditNoteUseCase,
                                refundedOrderUseCases: RefundedOrderUseCases) =
        OrderRefundService(refundOrderUseCase, issueCreditNoteUseCase, refundedOrderUseCases)

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
    open fun signInService(userRepository: UserRepository,
                           recordFailedSignInUseCase: RecordFailedSignInUseCase,
                           resetFailedSignInsUseCase: ResetFailedSignInsUseCase,
                           twoFactorChallengeRepository: TwoFactorChallengeRepository) =
        SignInService(
            userRepository, recordFailedSignInUseCase, resetFailedSignInsUseCase, twoFactorChallengeRepository
        )

    @Bean
    open fun userSessionService(
        openSessionUseCase: OpenSessionUseCase,
        readRefreshSessionUseCase: ReadRefreshSessionUseCase,
        rotateRefreshSessionUseCase: RotateRefreshSessionUseCase,
        revokeRefreshSessionUseCase: RevokeRefreshSessionUseCase,
        revokeAllRefreshSessionsUseCase: RevokeAllRefreshSessionsUseCase,
        readAccountUseCase: ReadAccountUseCase
    ) = UserSessionService(
        openSessionUseCase, readRefreshSessionUseCase, rotateRefreshSessionUseCase, revokeRefreshSessionUseCase,
        revokeAllRefreshSessionsUseCase, readAccountUseCase
    )

    @Bean
    open fun userService(userRepository: UserRepository): UserService {
        return UserService(userRepository)
    }

    @Bean
    open fun twoFactorAuthenticationService(
        readAccountUseCase: ReadAccountUseCase,
        startTwoFactorEnrollmentUseCase: StartTwoFactorEnrollmentUseCase,
        confirmTwoFactorEnrollmentUseCase: ConfirmTwoFactorEnrollmentUseCase,
        disableTwoFactorAuthenticationUseCase: DisableTwoFactorAuthenticationUseCase,
        adminDisableTwoFactorAuthenticationUseCase: AdminDisableTwoFactorAuthenticationUseCase
    ) = TwoFactorAuthenticationService(
        readAccountUseCase, startTwoFactorEnrollmentUseCase, confirmTwoFactorEnrollmentUseCase,
        disableTwoFactorAuthenticationUseCase, adminDisableTwoFactorAuthenticationUseCase
    )
}
