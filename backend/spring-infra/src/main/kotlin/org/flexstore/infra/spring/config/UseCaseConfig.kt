package org.flexstore.infra.spring.config

import org.flexstore.domain.payment.*
import org.flexstore.domain.payment.usecase.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.email.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.discount.*
import org.flexstore.domain.discount.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.image.*
import org.flexstore.domain.image.usecase.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.*
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases, each one a bean that receives its repositories (the adapters of the ports, @Repository)
@Configuration
open class UseCaseConfig(private val shopUrl: ShopUrl) {

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }

    @Bean
    open fun updateProductUseCase(productRepository: ProductRepository): UpdateProductUseCase {
        return UpdateProductUseCase(productRepository)
    }

    @Bean
    open fun readProductUseCase(productRepository: ProductRepository): ReadProductUseCase {
        return ReadProductUseCase(productRepository)
    }

    @Bean
    open fun listProductsUseCase(productRepository: ProductRepository) = ListProductsUseCase(productRepository)

    @Bean
    open fun addItemToCartUseCase(cartRepository: CartRepository,
                                  productRepository: ProductRepository): AddItemToCartUseCase {
        return AddItemToCartUseCase(cartRepository, productRepository)
    }

    @Bean
    open fun changeCartItemQuantityUseCase(cartRepository: CartRepository): ChangeCartItemQuantityUseCase {
        return ChangeCartItemQuantityUseCase(cartRepository)
    }

    @Bean
    open fun readCartUseCase(cartRepository: CartRepository): ReadCartUseCase {
        return ReadCartUseCase(cartRepository)
    }

    @Bean
    open fun mergeCartsUseCase(cartRepository: CartRepository): MergeCartsUseCase {
        return MergeCartsUseCase(cartRepository)
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
    open fun readShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): ReadShopSettingsUseCase {
        return ReadShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun updateShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository):
        UpdateShopSettingsUseCase {
        return UpdateShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun keepOrderAddressesUseCase(userRepository: UserRepository) =
        KeepOrderAddressesUseCase(userRepository)

    @Bean
    open fun placeOrderUseCase(orderRepository: OrderRepository, stockRepository: StockRepository) =
        PlaceOrderUseCase(orderRepository, stockRepository)

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
    open fun listCustomerOrdersUseCase(orderRepository: OrderRepository) =
        ListCustomerOrdersUseCase(orderRepository)

    @Bean
    open fun listAllOrdersUseCase(orderRepository: OrderRepository) = ListAllOrdersUseCase(orderRepository)

    @Bean
    open fun readAnyOrderUseCase(orderRepository: OrderRepository) = ReadAnyOrderUseCase(orderRepository)

    @Bean
    open fun readCustomerOrderUseCase(orderRepository: OrderRepository) =
        ReadCustomerOrderUseCase(orderRepository)

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
    open fun advanceOrderStatusUseCase(orderRepository: OrderRepository): AdvanceOrderStatusUseCase {
        return AdvanceOrderStatusUseCase(orderRepository)
    }

    @Bean
    open fun changePostalAddressUseCase(userRepository: UserRepository) =
        ChangePostalAddressUseCase(userRepository)

    @Bean
    open fun removePostalAddressUseCase(userRepository: UserRepository) =
        RemovePostalAddressUseCase(userRepository)

    @Bean
    open fun addressUseCases(changePostalAddressUseCase: ChangePostalAddressUseCase,
                             removePostalAddressUseCase: RemovePostalAddressUseCase) =
        AddressUseCases(changePostalAddressUseCase, removePostalAddressUseCase)

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
    open fun proposeSkuUseCase(productRepository: ProductRepository) = ProposeSkuUseCase(productRepository)

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
        shopSettingsRepository: ShopSettingsRepository
    ) = ProposeShippingOptionsUseCase(cartRepository, shopSettingsRepository)

    @Bean
    open fun readStockUseCase(stockRepository: StockRepository) = ReadStockUseCase(stockRepository)

    @Bean
    open fun listStocksUseCase(stockRepository: StockRepository) = ListStocksUseCase(stockRepository)

    @Bean
    open fun changeStockUseCase(stockRepository: StockRepository, productRepository: ProductRepository) =
        ChangeStockUseCase(stockRepository, productRepository)

    @Bean
    open fun stockUseCases(listStocksUseCase: ListStocksUseCase,
                           readStockUseCase: ReadStockUseCase,
                           changeStockUseCase: ChangeStockUseCase) =
        StockUseCases(listStocksUseCase, readStockUseCase, changeStockUseCase)

    @Bean
    open fun addProductImageUseCase(productImageRepository: ProductImageRepository,
                                    productRepository: ProductRepository,
                                    imageResizerRepository: ImageResizerRepository) =
        AddProductImageUseCase(productImageRepository, productRepository, imageResizerRepository)

    @Bean
    open fun removeProductImageUseCase(productImageRepository: ProductImageRepository) =
        RemoveProductImageUseCase(productImageRepository)

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
    open fun readSellerUseCase(sellerRepository: SellerRepository) = ReadSellerUseCase(sellerRepository)

    @Bean
    open fun readCurrentSellerUseCase(sellerRepository: SellerRepository) =
        ReadCurrentSellerUseCase(sellerRepository)

    @Bean
    open fun updateSellerUseCase(sellerRepository: SellerRepository) = UpdateSellerUseCase(sellerRepository)

    @Bean
    open fun sellerUseCases(readCurrentSellerUseCase: ReadCurrentSellerUseCase,
                            updateSellerUseCase: UpdateSellerUseCase) =
        SellerUseCases(readCurrentSellerUseCase, updateSellerUseCase)

    @Bean
    open fun renderInvoiceUseCase(invoiceRenderer: InvoiceRenderer) = RenderInvoiceUseCase(invoiceRenderer)

    @Bean
    open fun invoiceUseCases(readInvoiceUseCase: ReadInvoiceUseCase,
                             readSellerUseCase: ReadSellerUseCase,
                             renderInvoiceUseCase: RenderInvoiceUseCase) =
        InvoiceUseCases(readInvoiceUseCase, readSellerUseCase, renderInvoiceUseCase)

    @Bean
    open fun createDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        CreateDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun listDiscountCodesUseCase(discountCodeRepository: DiscountCodeRepository) =
        ListDiscountCodesUseCase(discountCodeRepository)

    @Bean
    open fun deleteDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        DeleteDiscountCodeUseCase(discountCodeRepository)

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
}
