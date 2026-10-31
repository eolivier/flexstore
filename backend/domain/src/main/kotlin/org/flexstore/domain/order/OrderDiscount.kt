package org.flexstore.domain.order

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

// The discount of an order, fixed when it is placed: none, or the code entered and the amount it took off
sealed interface OrderDiscount {
    fun amountIn(currency: Currency): Price
}

data object NoOrderDiscount : OrderDiscount {
    override fun amountIn(currency: Currency) = Price(Amount(BigDecimal.ZERO), currency)
}

data class AppliedOrderDiscount(val code: PromoCode, val amount: Price) : OrderDiscount {
    override fun amountIn(currency: Currency) = amount

    // How the customer reads it, on the payment page, the emails and the invoice (in French, the language of the
    // shop)
    fun label() = DiscountLabel("Remise ${code.value}")
}

// The name of the discount of an order, as the customer reads it
data class DiscountLabel(val value: String)

// The discount code an order is placed with: none, or a code found usable when the order is placed
sealed interface DiscountChoice

data object WithoutDiscountCode : DiscountChoice

data class WithDiscountCode(val discountCode: DiscountCode) : DiscountChoice

// The discount code a customer enters when checking out: none, or a code (read usable before the order is
// placed)
sealed interface EnteredDiscountCode

data object NoDiscountCodeEntered : EnteredDiscountCode

data class DiscountCodeEntered(val code: PromoCode) : EnteredDiscountCode

// What a discount code entered takes off the cart of the customer, before they order
data class DiscountPreview(val code: PromoCode, val amountOff: Price)
