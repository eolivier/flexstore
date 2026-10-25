package org.flexstore.infra.spring.adapter.payment

import com.stripe.model.Customer
import com.stripe.model.checkout.Session
import com.stripe.net.RequestOptions
import com.stripe.param.CustomerCreateParams
import com.stripe.param.CustomerListParams
import com.stripe.param.CustomerUpdateParams
import com.stripe.param.checkout.SessionCreateParams
import com.stripe.param.checkout.SessionCreateParams.LineItem
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData
import org.flexstore.domain.order.OrderLine
import org.flexstore.domain.payment.*
import java.math.BigDecimal

// Stripe Checkout: the customer pays on the page of Stripe (the card never reaches the shop),
// then comes back to the confirmation page of the order. Stripe confirms the payment later, with its webhook.
// The page is prefilled through a Stripe customer: the email of the payer and the name of the cardholder
class StripePaymentProvider(private val secretKey: String, private val frontUrl: String) : PaymentProvider {

    private val requestOptions = RequestOptions.builder().setApiKey(secretKey).build()

    override fun checkout(paymentRequest: PaymentRequest): ProviderCheckout {
        val order = paymentRequest.order
        val orderPage = "${frontUrl.trimEnd('/')}/orders/${order.orderId.value}"
        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setClientReferenceId(order.orderId.value)
            .setCustomer(stripeCustomerOf(paymentRequest.payer).id)
            .setSuccessUrl("$orderPage/confirmation")
            .setCancelUrl(orderPage)
            .addAllLineItem(order.lines.asList().map { it.toLineItem() })
            .build()
        val session = Session.create(params, requestOptions)
        return ProviderCheckout(session.id, CheckoutPage(session.url), PaymentOutcome.PENDING)
    }

    // One Stripe customer per email, named after the cardholder of this payment (no name when unknown)
    private fun stripeCustomerOf(payer: Payer): Customer {
        val cardholderName = when (val cardholder = payer.cardholder) {
            is Cardholder.Named -> cardholder.fullName.value
            Cardholder.Unknown -> ""
        }
        val listParams = CustomerListParams.builder().setEmail(payer.email.value).setLimit(1L).build()
        val existingCustomer = Customer.list(listParams, requestOptions).data.firstOrNull()
        if (existingCustomer == null) {
            val createParams = CustomerCreateParams.builder().setEmail(payer.email.value)
            if (cardholderName.isNotEmpty()) createParams.setName(cardholderName)
            return Customer.create(createParams.build(), requestOptions)
        }
        // An empty name removes the name of a previous payment
        val updateParams = CustomerUpdateParams.builder().setName(cardholderName).build()
        return existingCustomer.update(updateParams, requestOptions)
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
