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
    open fun placeOrderUseCase(cartRepository: CartRepository,
                               orderRepository: OrderRepository,
                               userRepository: UserRepository,
                               stockRepository: StockRepository): PlaceOrderUseCase {
        return PlaceOrderUseCase(cartRepository, orderRepository, userRepository, stockRepository)
    }

    @Bean
    open fun readOrdersUseCase(orderRepository: OrderRepository): ReadOrdersUseCase {
        return ReadOrdersUseCase(orderRepository)
    }

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
    open fun readAccountUseCase(userRepository: UserRepository): ReadAccountUseCase {
        return ReadAccountUseCase(userRepository)
    }

    // The groups of use cases of a sub-domain
    @Bean
    open fun productUseCases(createProductUseCase: CreateProductUseCase,
                             readProductUseCase: ReadProductUseCase,
                             updateProductUseCase: UpdateProductUseCase) =
        ProductUseCases(createProductUseCase, readProductUseCase, updateProductUseCase)

    @Bean
    open fun cartUseCases(addItemToCartUseCase: AddItemToCartUseCase,
                          changeCartItemQuantityUseCase: ChangeCartItemQuantityUseCase,
                          readCartUseCase: ReadCartUseCase,
                          mergeCartsUseCase: MergeCartsUseCase) =
        CartUseCases(addItemToCartUseCase, changeCartItemQuantityUseCase, readCartUseCase, mergeCartsUseCase)

    @Bean
    open fun orderUseCases(placeOrderUseCase: PlaceOrderUseCase,
                           readOrdersUseCase: ReadOrdersUseCase,
                           sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase) =
        OrderUseCases(placeOrderUseCase, readOrdersUseCase, sendOrderConfirmationEmailUseCase)

    @Bean
    open fun readStockUseCase(stockRepository: StockRepository) = ReadStockUseCase(stockRepository)

    @Bean
    open fun changeStockUseCase(stockRepository: StockRepository, productRepository: ProductRepository) =
        ChangeStockUseCase(stockRepository, productRepository)

    @Bean
    open fun stockUseCases(readStockUseCase: ReadStockUseCase, changeStockUseCase: ChangeStockUseCase) =
        StockUseCases(readStockUseCase, changeStockUseCase)

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
    open fun productImageUseCases(addProductImageUseCase: AddProductImageUseCase,
                                  removeProductImageUseCase: RemoveProductImageUseCase,
                                  readProductImageUseCase: ReadProductImageUseCase) =
        ProductImageUseCases(addProductImageUseCase, removeProductImageUseCase, readProductImageUseCase)
}
