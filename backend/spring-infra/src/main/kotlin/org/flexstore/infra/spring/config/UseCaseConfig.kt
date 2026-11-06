package org.flexstore.infra.spring.config

import org.flexstore.domain.email.model.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.cookie.CookieConsentRepository
import org.flexstore.domain.cookie.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.discount.*
import org.flexstore.domain.discount.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.service.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.usecase.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.image.*
import org.flexstore.domain.image.usecase.*
import org.flexstore.domain.legal.*
import org.flexstore.domain.legal.usecase.*
import org.flexstore.domain.review.*
import org.flexstore.domain.review.usecase.*
import org.flexstore.domain.sales.usecase.*
import org.flexstore.domain.environment.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.*
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.usecase.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.*
import org.flexstore.domain.user.service.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.wishlist.*
import org.flexstore.domain.wishlist.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.flexstore.domain.user.address.AddressUseCases
import org.flexstore.domain.user.address.usecase.AddPostalAddressUseCase
import org.flexstore.domain.user.address.usecase.ChangePostalAddressUseCase
import org.flexstore.domain.user.address.usecase.RemovePostalAddressUseCase

// The use cases, each one a bean that receives its repositories (the adapters of the ports, @Repository)
@Configuration
open class UseCaseConfig(private val shopUrl: ShopUrl) {

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository,
                                  userRepository: UserRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository, userRepository)
    }

    @Bean
    open fun updateProductUseCase(productRepository: ProductRepository,
                                  userRepository: UserRepository): UpdateProductUseCase {
        return UpdateProductUseCase(productRepository, userRepository)
    }

    @Bean
    open fun readProductUseCase(productRepository: ProductRepository): ReadProductUseCase {
        return ReadProductUseCase(productRepository)
    }

    @Bean
    open fun listProductsUseCase(productRepository: ProductRepository) = ListProductsUseCase(productRepository)

    @Bean
    open fun addItemToCartUseCase(cartRepository: CartRepository,
                                  productRepository: ProductRepository,
                                  userRepository: UserRepository): AddItemToCartUseCase {
        return AddItemToCartUseCase(cartRepository, productRepository, userRepository)
    }

    @Bean
    open fun changeCartItemQuantityUseCase(cartRepository: CartRepository,
                                           userRepository: UserRepository): ChangeCartItemQuantityUseCase {
        return ChangeCartItemQuantityUseCase(cartRepository, userRepository)
    }

    @Bean
    open fun readCartUseCase(cartRepository: CartRepository, userRepository: UserRepository): ReadCartUseCase {
        return ReadCartUseCase(cartRepository, userRepository)
    }

    @Bean
    open fun mergeCartsUseCase(cartRepository: CartRepository,
                               userRepository: UserRepository): MergeCartsUseCase {
        return MergeCartsUseCase(cartRepository, userRepository)
    }

    @Bean
    open fun startPaymentUseCase(orderRepository: OrderRepository,
                                 paymentRepository: PaymentRepository,
                                 userRepository: UserRepository) =
        StartPaymentUseCase(orderRepository, paymentRepository, userRepository)

    @Bean
    open fun confirmPaymentUseCase(orderRepository: OrderRepository,
                                   cartRepository: CartRepository,
                                   stockRepository: StockRepository) =
        ConfirmPaymentUseCase(orderRepository, cartRepository, stockRepository)

    @Bean
    open fun readPaymentUseCase(paymentRepository: PaymentRepository) = ReadPaymentUseCase(paymentRepository)

    @Bean
    open fun paymentUseCases(startPaymentUseCase: StartPaymentUseCase,
                             confirmPaymentUseCase: ConfirmPaymentUseCase,
                             readPaymentUseCase: ReadPaymentUseCase) =
        PaymentUseCases(startPaymentUseCase, confirmPaymentUseCase, readPaymentUseCase)


    @Bean
    open fun sendOrderConfirmationEmailUseCase(shopSettingsRepository: ShopSettingsRepository,
                                               userRepository: UserRepository,
                                               emailRepository: EmailRepository) =
        SendOrderConfirmationEmailUseCase(shopSettingsRepository, userRepository, emailRepository)

    @Bean
    open fun listAbandonedCartsUseCase(cartRepository: CartRepository) =
        ListAbandonedCartsUseCase(cartRepository)

    @Bean
    open fun remindAbandonedCartUseCase(cartRepository: CartRepository,
                                        userRepository: UserRepository,
                                        emailRepository: EmailRepository) =
        RemindAbandonedCartUseCase(cartRepository, userRepository, emailRepository)

    @Bean
    open fun readShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): ReadShopSettingsUseCase {
        return ReadShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun readAdministeredShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository,
                                                 userRepository: UserRepository) =
        ReadAdministeredShopSettingsUseCase(shopSettingsRepository, userRepository)

    @Bean
    open fun updateShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository,
                                       userRepository: UserRepository) =
        UpdateShopSettingsUseCase(shopSettingsRepository, userRepository)

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
    open fun listCustomerOrdersUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ListCustomerOrdersUseCase(orderRepository, userRepository)

    @Bean
    open fun listAllOrdersUseCase(orderRepository: OrderRepository) = ListAllOrdersUseCase(orderRepository)

    @Bean
    open fun readAnyOrderUseCase(orderRepository: OrderRepository) = ReadAnyOrderUseCase(orderRepository)

    @Bean
    open fun readCustomerOrderUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ReadCustomerOrderUseCase(orderRepository, userRepository)

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
    open fun refundOrderUseCase(
        orderRepository: OrderRepository,
        stockRepository: StockRepository,
        paymentRepository: PaymentRepository,
        userRepository: UserRepository
    ) = RefundOrderUseCase(orderRepository, stockRepository, paymentRepository, userRepository)

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
    open fun advanceOrderStatusUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        AdvanceOrderStatusUseCase(orderRepository, userRepository)

    @Bean
    open fun addPostalAddressUseCase(userRepository: UserRepository) =
        AddPostalAddressUseCase(userRepository)

    @Bean
    open fun correctOrderTrackingUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        CorrectOrderTrackingUseCase(orderRepository, userRepository)

    @Bean
    open fun changePostalAddressUseCase(userRepository: UserRepository) =
        ChangePostalAddressUseCase(userRepository)

    @Bean
    open fun removePostalAddressUseCase(userRepository: UserRepository) =
        RemovePostalAddressUseCase(userRepository)

    @Bean
    open fun addressUseCases(addPostalAddressUseCase: AddPostalAddressUseCase,
                             changePostalAddressUseCase: ChangePostalAddressUseCase,
                             removePostalAddressUseCase: RemovePostalAddressUseCase) =
        AddressUseCases(addPostalAddressUseCase, changePostalAddressUseCase, removePostalAddressUseCase)

    @Bean
    open fun registerCustomerUseCase(userRepository: UserRepository): RegisterCustomerUseCase {
        return RegisterCustomerUseCase(userRepository)
    }

    @Bean
    // The links of the verification emails open the page /verify-email of the front
    open fun sendVerificationEmailUseCase(userRepository: UserRepository,
                                          emailRepository: EmailRepository): SendVerificationEmailUseCase {
        val verificationPage = VerificationPage.of(shopUrl)
        return SendVerificationEmailUseCase(userRepository, emailRepository, verificationPage)
    }

    @Bean
    open fun verifyEmailUseCase(userRepository: UserRepository,
                                emailRepository: EmailRepository): VerifyEmailUseCase {
        return VerifyEmailUseCase(userRepository, emailRepository)
    }

    @Bean
    open fun requestPasswordResetUseCase(userRepository: UserRepository,
                                         passwordResetRepository: PasswordResetRepository,
                                         emailRepository: EmailRepository) =
        RequestPasswordResetUseCase(userRepository, passwordResetRepository, emailRepository)

    @Bean
    open fun resetPasswordUseCase(userRepository: UserRepository,
                                  passwordResetRepository: PasswordResetRepository) =
        ResetPasswordUseCase(userRepository, passwordResetRepository)

    @Bean
    open fun cancelUnpaidOrderUseCase(orderRepository: OrderRepository,
                                      stockRepository: StockRepository,
                                      paymentRepository: PaymentRepository) =
        CancelUnpaidOrderUseCase(orderRepository, stockRepository, paymentRepository)

    @Bean
    open fun readAccountUseCase(userRepository: UserRepository): ReadAccountUseCase {
        return ReadAccountUseCase(userRepository)
    }

    @Bean
    open fun recordFailedSignInUseCase(userRepository: UserRepository,
                                       failedSignInRepository: FailedSignInRepository) =
        RecordFailedSignInUseCase(userRepository, failedSignInRepository)

    @Bean
    open fun resetFailedSignInsUseCase(failedSignInRepository: FailedSignInRepository) =
        ResetFailedSignInsUseCase(failedSignInRepository)

    // The groups of use cases of a sub-domain
    @Bean
    open fun productUseCases(createProductUseCase: CreateProductUseCase,
                             readProductUseCase: ReadProductUseCase,
                             listProductsUseCase: ListProductsUseCase,
                             updateProductUseCase: UpdateProductUseCase,
                             proposeSkuUseCase: ProposeSkuUseCase) =
        ProductUseCases(
            createProductUseCase,
            readProductUseCase,
            listProductsUseCase,
            updateProductUseCase,
            proposeSkuUseCase
        )

    @Bean
    open fun proposeSkuUseCase(productRepository: ProductRepository, userRepository: UserRepository) =
        ProposeSkuUseCase(productRepository, userRepository)

    @Bean
    open fun cartUseCases(addItemToCartUseCase: AddItemToCartUseCase,
                          changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                          readCartUseCase: ReadCartUseCase,
                          mergeCartsUseCase: MergeCartsUseCase) =
        CartUseCases(addItemToCartUseCase, changeCartItemQuantityUseCase, readCartUseCase, mergeCartsUseCase)

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
    open fun proposeShippingOptionsUseCase(
        cartRepository: CartRepository,
        shopSettingsRepository: ShopSettingsRepository,
        userRepository: UserRepository
    ) = ProposeShippingOptionsUseCase(cartRepository, shopSettingsRepository, userRepository)

    @Bean
    open fun readStockUseCase(stockRepository: StockRepository) = ReadStockUseCase(stockRepository)

    @Bean
    open fun listStocksUseCase(stockRepository: StockRepository) = ListStocksUseCase(stockRepository)

    @Bean
    open fun changeStockUseCase(stockRepository: StockRepository,
                                productRepository: ProductRepository,
                                userRepository: UserRepository) =
        ChangeStockUseCase(stockRepository, productRepository, userRepository)

    @Bean
    open fun stockUseCases(listStocksUseCase: ListStocksUseCase,
                           readStockUseCase: ReadStockUseCase,
                           changeStockUseCase: ChangeStockUseCase) =
        StockUseCases(listStocksUseCase, readStockUseCase, changeStockUseCase)

    @Bean
    open fun listLowStocksUseCase(stockRepository: StockRepository, userRepository: UserRepository) =
        ListLowStocksUseCase(stockRepository, userRepository)

    @Bean
    open fun readStockAlertUseCase(stockRepository: StockRepository, userRepository: UserRepository) =
        ReadStockAlertUseCase(stockRepository, userRepository)

    @Bean
    open fun changeStockAlertUseCase(stockRepository: StockRepository,
                                     productRepository: ProductRepository,
                                     userRepository: UserRepository) =
        ChangeStockAlertUseCase(stockRepository, productRepository, userRepository)

    @Bean
    open fun stockAlertUseCases(listLowStocksUseCase: ListLowStocksUseCase,
                                readStockAlertUseCase: ReadStockAlertUseCase,
                                changeStockAlertUseCase: ChangeStockAlertUseCase) =
        StockAlertUseCases(listLowStocksUseCase, readStockAlertUseCase, changeStockAlertUseCase)

    @Bean
    open fun addProductImageUseCase(productImageRepository: ProductImageRepository,
                                    productRepository: ProductRepository,
                                    imageResizerRepository: ImageResizerRepository,
                                    userRepository: UserRepository) =
        AddProductImageUseCase(productImageRepository, productRepository, imageResizerRepository, userRepository)

    @Bean
    open fun removeProductImageUseCase(productImageRepository: ProductImageRepository,
                                       userRepository: UserRepository) =
        RemoveProductImageUseCase(productImageRepository, userRepository)

    @Bean
    open fun readProductImageUseCase(productImageRepository: ProductImageRepository) =
        ReadProductImageUseCase(productImageRepository)

    @Bean
    open fun listProductsWithImageUseCase(productImageRepository: ProductImageRepository) =
        ListProductsWithImageUseCase(productImageRepository)

    @Bean
    open fun productImageUseCases(addProductImageUseCase: AddProductImageUseCase,
                                  removeProductImageUseCase: RemoveProductImageUseCase,
                                  readProductImageUseCase: ReadProductImageUseCase,
                                  listProductsWithImageUseCase: ListProductsWithImageUseCase) =
        ProductImageUseCases(
            addProductImageUseCase,
            removeProductImageUseCase,
            readProductImageUseCase,
            listProductsWithImageUseCase
        )

    @Bean
    open fun issueInvoiceUseCase(orderRepository: OrderRepository,
                                 invoiceRepository: InvoiceRepository,
                                 sellerRepository: SellerRepository) =
        IssueInvoiceUseCase(orderRepository, invoiceRepository, sellerRepository)

    @Bean
    open fun readInvoiceUseCase(invoiceRepository: InvoiceRepository) = ReadInvoiceUseCase(invoiceRepository)

    @Bean
    open fun issueCreditNoteUseCase(orderRepository: OrderRepository,
                                    invoiceRepository: InvoiceRepository,
                                    creditNoteRepository: CreditNoteRepository,
                                    userRepository: UserRepository) =
        IssueCreditNoteUseCase(orderRepository, invoiceRepository, creditNoteRepository, userRepository)

    @Bean
    open fun readCreditNoteUseCase(creditNoteRepository: CreditNoteRepository) =
        ReadCreditNoteUseCase(creditNoteRepository)

    @Bean
    open fun refundedOrderUseCases(readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
                                   sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase) =
        RefundedOrderUseCases(readAdministeredOrderUseCase, sendOrderStatusEmailUseCase)

    @Bean
    open fun readSellerUseCase(sellerRepository: SellerRepository) = ReadSellerUseCase(sellerRepository)

    @Bean
    open fun readCurrentSellerUseCase(sellerRepository: SellerRepository, userRepository: UserRepository) =
        ReadCurrentSellerUseCase(sellerRepository, userRepository)

    @Bean
    open fun updateSellerUseCase(sellerRepository: SellerRepository, userRepository: UserRepository) =
        UpdateSellerUseCase(sellerRepository, userRepository)

    @Bean
    open fun readSocialNetworksUseCase(sellerRepository: SellerRepository) =
        ReadSocialNetworksUseCase(sellerRepository)

    @Bean
    open fun sellerUseCases(readCurrentSellerUseCase: ReadCurrentSellerUseCase,
                            updateSellerUseCase: UpdateSellerUseCase,
                            readSocialNetworksUseCase: ReadSocialNetworksUseCase) =
        SellerUseCases(readCurrentSellerUseCase, updateSellerUseCase, readSocialNetworksUseCase)

    @Bean
    open fun readLegalPageUseCase(legalPageRepository: LegalPageRepository) =
        ReadLegalPageUseCase(legalPageRepository)

    @Bean
    open fun publishLegalPageUseCase(legalPageRepository: LegalPageRepository, userRepository: UserRepository) =
        PublishLegalPageUseCase(legalPageRepository, userRepository)

    @Bean
    open fun legalPageUseCases(readLegalPageUseCase: ReadLegalPageUseCase,
                               publishLegalPageUseCase: PublishLegalPageUseCase) =
        LegalPageUseCases(readLegalPageUseCase, publishLegalPageUseCase)

    @Bean
    open fun renderInvoiceUseCase(invoiceRenderer: InvoiceRenderer) = RenderInvoiceUseCase(invoiceRenderer)

    @Bean
    open fun invoiceUseCases(readInvoiceUseCase: ReadInvoiceUseCase,
                             readCreditNoteUseCase: ReadCreditNoteUseCase,
                             readSellerUseCase: ReadSellerUseCase,
                             renderInvoiceUseCase: RenderInvoiceUseCase) =
        InvoiceUseCases(readInvoiceUseCase, readCreditNoteUseCase, readSellerUseCase, renderInvoiceUseCase)

    @Bean
    open fun createDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository,
                                       userRepository: UserRepository) =
        CreateDiscountCodeUseCase(discountCodeRepository, userRepository)

    @Bean
    open fun listDiscountCodesUseCase(discountCodeRepository: DiscountCodeRepository,
                                      userRepository: UserRepository) =
        ListDiscountCodesUseCase(discountCodeRepository, userRepository)

    @Bean
    open fun deleteDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository,
                                       userRepository: UserRepository) =
        DeleteDiscountCodeUseCase(discountCodeRepository, userRepository)

    @Bean
    open fun readUsableDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        ReadUsableDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun useDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        UseDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun discountCodeUseCases(createDiscountCodeUseCase: CreateDiscountCodeUseCase,
                                  listDiscountCodesUseCase: ListDiscountCodesUseCase,
                                  deleteDiscountCodeUseCase: DeleteDiscountCodeUseCase) =
        DiscountCodeUseCases(createDiscountCodeUseCase, listDiscountCodesUseCase, deleteDiscountCodeUseCase)

    @Bean
    open fun checkoutDiscountUseCases(readUsableDiscountCodeUseCase: ReadUsableDiscountCodeUseCase,
                                      useDiscountCodeUseCase: UseDiscountCodeUseCase) =
        CheckoutDiscountUseCases(readUsableDiscountCodeUseCase, useDiscountCodeUseCase)

    @Bean
    open fun addReviewUseCase(reviewRepository: ReviewRepository,
                              orderRepository: OrderRepository,
                              productRepository: ProductRepository,
                              userRepository: UserRepository) =
        AddReviewUseCase(reviewRepository, orderRepository, productRepository, userRepository)

    @Bean
    open fun listProductReviewsUseCase(reviewRepository: ReviewRepository) =
        ListProductReviewsUseCase(reviewRepository)

    @Bean
    open fun listRatingSummariesUseCase(reviewRepository: ReviewRepository) =
        ListRatingSummariesUseCase(reviewRepository)

    @Bean
    open fun deleteReviewUseCase(reviewRepository: ReviewRepository, userRepository: UserRepository) =
        DeleteReviewUseCase(reviewRepository, userRepository)

    @Bean
    open fun readReviewEligibilityUseCase(
        reviewRepository: ReviewRepository,
        orderRepository: OrderRepository,
        userRepository: UserRepository
    ) = ReadReviewEligibilityUseCase(reviewRepository, orderRepository, userRepository)

    @Bean
    open fun reviewUseCases(addReviewUseCase: AddReviewUseCase,
                            listProductReviewsUseCase: ListProductReviewsUseCase,
                            listRatingSummariesUseCase: ListRatingSummariesUseCase,
                            deleteReviewUseCase: DeleteReviewUseCase,
                            readReviewEligibilityUseCase: ReadReviewEligibilityUseCase) =
        ReviewUseCases(
            addReviewUseCase, listProductReviewsUseCase, listRatingSummariesUseCase, deleteReviewUseCase,
            readReviewEligibilityUseCase
        )

    @Bean
    open fun readCookieConsentUseCase(
        cookieConsentRepository: CookieConsentRepository,
        userRepository: UserRepository
    ) = ReadCookieConsentUseCase(cookieConsentRepository, userRepository)

    @Bean
    open fun recordCookieConsentUseCase(
        cookieConsentRepository: CookieConsentRepository,
        userRepository: UserRepository
    ) = RecordCookieConsentUseCase(cookieConsentRepository, userRepository)

    @Bean
    open fun cookieConsentUseCases(
        readCookieConsentUseCase: ReadCookieConsentUseCase,
        recordCookieConsentUseCase: RecordCookieConsentUseCase
    ) = CookieConsentUseCases(readCookieConsentUseCase, recordCookieConsentUseCase)

    @Bean
    open fun openSessionUseCase(
        refreshSessionRepository: RefreshSessionRepository,
        userRepository: UserRepository
    ) = OpenSessionUseCase(refreshSessionRepository, userRepository)

    @Bean
    open fun readRefreshSessionUseCase(refreshSessionRepository: RefreshSessionRepository) =
        ReadRefreshSessionUseCase(refreshSessionRepository)

    @Bean
    open fun rotateRefreshSessionUseCase(
        refreshSessionRepository: RefreshSessionRepository,
        userRepository: UserRepository
    ) = RotateRefreshSessionUseCase(refreshSessionRepository, userRepository)

    @Bean
    open fun revokeRefreshSessionUseCase(refreshSessionRepository: RefreshSessionRepository) =
        RevokeRefreshSessionUseCase(refreshSessionRepository)

    @Bean
    open fun revokeAllRefreshSessionsUseCase(refreshSessionRepository: RefreshSessionRepository) =
        RevokeAllRefreshSessionsUseCase(refreshSessionRepository)

    @Bean
    open fun readEnvironmentUseCase(environmentRepository: EnvironmentRepository, userRepository: UserRepository) =
        ReadEnvironmentUseCase(environmentRepository, userRepository)

    @Bean
    open fun readSalesDashboardUseCase(orderRepository: OrderRepository, userRepository: UserRepository) =
        ReadSalesDashboardUseCase(orderRepository, userRepository)

    @Bean
    open fun startTwoFactorEnrollmentUseCase(userRepository: UserRepository) =
        StartTwoFactorEnrollmentUseCase(userRepository)

    @Bean
    open fun confirmTwoFactorEnrollmentUseCase(userRepository: UserRepository) =
        ConfirmTwoFactorEnrollmentUseCase(userRepository)

    @Bean
    open fun disableTwoFactorAuthenticationUseCase(userRepository: UserRepository) =
        DisableTwoFactorAuthenticationUseCase(userRepository)

    @Bean
    open fun adminDisableTwoFactorAuthenticationUseCase(userRepository: UserRepository) =
        AdminDisableTwoFactorAuthenticationUseCase(userRepository)

    @Bean
    open fun addProductToWishlistUseCase(
        wishlistRepository: WishlistRepository,
        productRepository: ProductRepository,
        userRepository: UserRepository
    ) = AddProductToWishlistUseCase(wishlistRepository, productRepository, userRepository)

    @Bean
    open fun removeProductFromWishlistUseCase(
        wishlistRepository: WishlistRepository,
        userRepository: UserRepository
    ) = RemoveProductFromWishlistUseCase(wishlistRepository, userRepository)

    @Bean
    open fun listWishlistUseCase(wishlistRepository: WishlistRepository, userRepository: UserRepository) =
        ListWishlistUseCase(wishlistRepository, userRepository)

    @Bean
    open fun wishlistUseCases(
        addProductToWishlistUseCase: AddProductToWishlistUseCase,
        removeProductFromWishlistUseCase: RemoveProductFromWishlistUseCase,
        listWishlistUseCase: ListWishlistUseCase
    ) = WishlistUseCases(addProductToWishlistUseCase, removeProductFromWishlistUseCase, listWishlistUseCase)
}
