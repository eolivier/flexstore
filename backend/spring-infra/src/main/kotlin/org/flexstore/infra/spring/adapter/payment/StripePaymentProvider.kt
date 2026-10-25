package org.flexstore.infra.spring.adapter.payment

import com.stripe.model.checkout.Session
import com.stripe.net.RequestOptions
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.LineItem
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData
import org.flexstore.domain.order.Order
import org.flexstore.domain.order.OrderLine
import org.flexstore.domain.payment.CheckoutPage
import org.flexstore.domain.payment.PaymentOutcome
import java.math.BigDecimal

// Stripe Checkout: the customer pays on the page of Stripe (the card never reaches the shop),
// then comes back to the confirmation page of the order. Stripe confirms the payment later, with its webhook
class StripePaymentProvider(private val secretKey: String, private val frontUrl: String) : PaymentProvider {

    override fun checkout(order: Order): ProviderCheckout {
        val orderPage = "${frontUrl.trimEnd('/')}/orders/${order.orderId.value}"
        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setClientReferenceId(order.orderId.value)
            .setSuccessUrl("$orderPage/confirmation")
            .setCancelUrl(orderPage)
            .addAllLineItem(order.lines.asList().map { it.toLineItem() })
            .build()
        val requestOptions = RequestOptions.builder().setApiKey(secretKey).build()
        val session = Session.create(params, requestOptions)
        return ProviderCheckout(session.id, CheckoutPage(session.url), PaymentOutcome.PENDING)
    }

    private fun OrderLine.toLineItem(): LineItem {
        val productData = PriceData.ProductData.builder().setName(productName.value).build()
        val unitAmountInCents = unitPrice.amount.value.multiply(BigDecimal(100)).longValueExact()
        val priceData = PriceData.builder()
            .setCurrency(unitPrice.currency.name.lowercase())
            .setUnitAmount(unitAmountInCents)
            .setProductData(productData)
            .build()
        return LineItem.builder().setQuantity(quantity.value.toLong()).setPriceData(priceData).build()
    }
}
