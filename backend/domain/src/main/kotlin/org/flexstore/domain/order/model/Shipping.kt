package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.discount.*
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor
import java.math.BigDecimal

// The identity of a shipping method
data class ShippingMethodId(override val id: Identity) : Identifiable() {
    companion object {
        fun nextId() = ShippingMethodId(Identity.nextId())
    }
}

// Whether the shipping of a method is free from the threshold of the shop settings (the standard one) or always
// at its cost (the express one)
enum class FreeShippingEligibility { ELIGIBLE, NOT_ELIGIBLE }

// A method that is disabled is no longer proposed to the customers; the orders placed with it keep it
enum class ShippingMethodStatus { ENABLED, DISABLED }

// How an order is shipped, chosen by the customer among the methods the administrators manage: its name, its cost
// and whether it is free from the threshold of the shop settings
data class ShippingMethod(
    val id: ShippingMethodId,
    private val label: ShippingLabel,
    val cost: Price,
    val eligibility: FreeShippingEligibility,
    val status: ShippingMethodStatus
) {

    // A new method, enabled
    constructor(id: ShippingMethodId, label: ShippingLabel, cost: Price, eligibility: FreeShippingEligibility) :
        this(id, label, cost, eligibility, ShippingMethodStatus.ENABLED)

    // How the customer reads it, on the payment page and in the emails
    fun label() = label

    fun isEnabled() = status == ShippingMethodStatus.ENABLED

    fun isDisabled() = !isEnabled()

    // A method has a name, and a cost that is not negative
    fun isInvalid() = label.isBlank() || cost.amount.value.signum() < 0

    // What the shipping costs for the items ordered
    fun costFor(itemsPrice: Price, freeShippingThreshold: FreeShippingThreshold): Price {
        val isFree = eligibility == FreeShippingEligibility.ELIGIBLE && freeShippingThreshold.isReachedBy(itemsPrice)
        val shippingCost = if (isFree) BigDecimal.ZERO else cost.amount.value
        return Price(Amount(shippingCost), itemsPrice.currency)
    }

    companion object {
        // The methods of a shop that has not changed them: the first two methods stored by the migration
        val STANDARD = ShippingMethod(
            ShippingMethodId(Identity("STANDARD")), ShippingLabel("Livraison standard"),
            Price(Amount(BigDecimal("4.90")), Currency.EUR), FreeShippingEligibility.ELIGIBLE
        )
        val EXPRESS = ShippingMethod(
            ShippingMethodId(Identity("EXPRESS")), ShippingLabel("Livraison express"),
            Price(Amount(BigDecimal("9.90")), Currency.EUR), FreeShippingEligibility.NOT_ELIGIBLE
        )
    }
}

// The shipping methods, in the order given
data class ShippingMethods(private val methods: List<ShippingMethod>) {

    constructor(vararg methods: ShippingMethod) : this(methods.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<ShippingMethod> = methods.toList()

    // The methods the customers can choose
    fun enabled() = ShippingMethods(methods.filter { it.isEnabled() })

    // The same methods, the one with the id of the given method replaced by it
    fun replacedBy(method: ShippingMethod) = ShippingMethods(methods.map { if (it.id == method.id) method else it })

    fun hasEnabledMethod() = methods.any { it.isEnabled() }

    fun hasNoEnabledMethod() = !hasEnabledMethod()

    companion object {
        // The shipping methods of a shop that has not changed them
        fun byDefault() = ShippingMethods(ShippingMethod.STANDARD, ShippingMethod.EXPRESS)
    }
}

// The name of a shipping method, as the customer reads it
data class ShippingLabel(val value: String) {
    fun isBlank() = value.isBlank()
}

// An administrator who adds a shipping method
data class ShippingMethodCreation(val creator: Actor, val method: ShippingMethod)

// An administrator who changes a shipping method: its name, its cost, whether it is free from the threshold, and
// whether it is enabled
data class ShippingMethodUpdate(val editor: Actor, val method: ShippingMethod)

// An administrator who reads all the shipping methods, the disabled ones included
data class ShippingMethodsReading(val reader: Actor)

// A customer who chooses a shipping method to check out
data class EnabledShippingMethodLookup(val customer: CartOwner.Customer, val shippingMethodId: ShippingMethodId)

data class ShippingMethodAdministrationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidShippingMethodException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class ShippingMethodNotFoundException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage) {
    constructor(message: String) : this(ErrorMessage(message))
}
data class ShippingMethodDisabledException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class NoShippingMethodLeftException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class ShippingMethodCreationFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)

// Where the order goes, and how
data class OrderDelivery(val addresses: OrderAddresses, val method: ShippingMethod)

// How the order is shipped, and what it costs, fixed when the order is placed
data class OrderShipping(val method: ShippingMethod, val cost: Price)

// What changes the price of the items of an order: its shipping, and its discount
data class OrderAdjustments(val shipping: OrderShipping, val discount: OrderDiscount) {

    // Without discount
    constructor(shipping: OrderShipping) : this(shipping, NoOrderDiscount)
}

// What the customer pays for an order: its lines, less its discount, then its shipping
data class OrderBill(val lines: OrderLines, val adjustments: OrderAdjustments) {

    val shipping get() = adjustments.shipping

    val discount get() = adjustments.discount

    fun itemsPrice() = lines.totalPrice()

    fun discountAmount() = discount.amountIn(itemsPrice().currency)

    fun totalPrice() = itemsPrice() - discountAmount() + shipping.cost
}

// The terms of the shop for an order: the threshold of free shipping, and the discount code chosen
data class OrderTerms(val freeShippingThreshold: FreeShippingThreshold, val discountChoice: DiscountChoice) {

    // Without discount code
    constructor(freeShippingThreshold: FreeShippingThreshold) : this(freeShippingThreshold, WithoutDiscountCode)
}

// What an order is placed from: the cart of the customer, and the terms of the shop
data class OrderContent(val cart: Cart, val terms: OrderTerms) {

    // Without discount code
    constructor(cart: Cart, freeShippingThreshold: FreeShippingThreshold) :
        this(cart, OrderTerms(freeShippingThreshold))

    val freeShippingThreshold get() = terms.freeShippingThreshold

    // The discount of the items: what the code chosen takes off their price
    fun discountOn(itemsPrice: Price): OrderDiscount = when (val discountChoice = terms.discountChoice) {
        WithoutDiscountCode -> NoOrderDiscount
        is WithDiscountCode -> {
            val discountCode = discountChoice.discountCode
            AppliedOrderDiscount(discountCode.code, discountCode.reduction.amountOff(itemsPrice))
        }
    }
}

// What a shipping method costs for the items of a cart
data class ShippingOption(val method: ShippingMethod, val cost: Price)

// The shipping methods a customer chooses from, with their cost for their cart, and the threshold from which the
// standard shipping is free
data class ShippingOptions(
    private val options: List<ShippingOption>,
    val freeShippingThreshold: FreeShippingThreshold
) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<ShippingOption> = options.toList()

    companion object {
        // The shipping methods the customers can choose, at their cost for these items
        fun forItems(
            shippingMethods: ShippingMethods,
            itemsPrice: Price,
            freeShippingThreshold: FreeShippingThreshold
        ): ShippingOptions {
            val options = shippingMethods.enabled().asList().map { method ->
                ShippingOption(method, method.costFor(itemsPrice, freeShippingThreshold))
            }
            return ShippingOptions(options, freeShippingThreshold)
        }
    }
}
