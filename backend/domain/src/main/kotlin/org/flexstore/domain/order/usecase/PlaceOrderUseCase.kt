package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

class PlaceOrderUseCase(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val stockRepository: StockRepository
) : UseCase<OrderPlacement> {

    override fun getPreConditions(): PreConditions<OrderPlacement> =
        PreConditions(
            customerIsKnownCondition(),
            accountAllowsOrderingCondition(),
            cartHasItemsCondition(),
            stockCoversTheCartCondition(),
            addressesAreCompleteCondition()
        )

    // The addresses of the order are kept among the addresses of the customer (an address entered when ordering
    // is
    // added), so that the order refers to them; then the order is placed. The cart is emptied once it is paid
    override fun getNominalScenario(): NominalScenario<OrderPlacement> {
        val keepAddressesStep = Step<OrderPlacement> { orderPlacement ->
            val definedUser = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
            val orderAddresses = orderPlacement.addresses
            val (delivery, billing) = orderAddresses
            val definedUserWithAddresses = definedUser.withPostalAddressesIncluding(delivery, billing)
            userRepository.save(definedUserWithAddresses)
        }
        val placeOrderStep = Step<OrderPlacement> { orderPlacement ->
            val cart = cartRepository.load(orderPlacement.customer)
            val order = Order.place(orderPlacement, cart)
            orderRepository.save(order)
        }
        // The units of the order are reserved until it is paid, or cancelled for lack of payment
        val reserveStockStep = Step<OrderPlacement> { orderPlacement ->
            val order = orderRepository.findById(orderPlacement.orderId)!!
            order.lines.asList().forEach { line ->
                stockRepository.save(stockRepository.stockOf(line.productId).withdrawn(line.quantity))
            }
        }
        return NominalScenario(listOf(keepAddressesStep, placeOrderStep, reserveStockStep))
    }

    override fun getPostConditions(): PostConditions<OrderPlacement> =
        PostConditions(orderIsStoredCondition())

    private fun customerIsKnownCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val userId = orderPlacement.customer.userId
        if (userRepository.notExists(userId)) {
            val message = ErrorMessage("The customer ${userId.value} is unknown: they cannot order.")
            throw UserNotFoundException(message)
        }
    }

    // A token stays valid after the account is suspended, locked or disabled: the status is checked at each
    // order
    private fun accountAllowsOrderingCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val definedUser = userRepository.findById(orderPlacement.customer.userId) as User.DefinedUser
        if (definedUser.status.refusesOrdering()) {
            val message = ErrorMessage("The account ${definedUser.id.value} is ${definedUser.status}: it cannot order.")
            throw OrderRefusedException(definedUser.status, message)
        }
    }

    private fun cartHasItemsCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        if (cartRepository.load(orderPlacement.customer).isEmpty()) {
            throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
        }
    }

    // The order reserves the units, so that they cannot be sold twice while it is paid
    private fun stockCoversTheCartCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        val cart = cartRepository.load(orderPlacement.customer)
        val missingProducts = cart.items.fold(emptyList<String>()) { missing, product, quantity ->
            val stock = stockRepository.stockOf(product.productId)
            if (stock.doesNotCover(quantity)) missing + product.name.value else missing
        }
        if (missingProducts.isNotEmpty()) {
            val message = ErrorMessage("Not enough units in stock for: ${missingProducts.joinToString()}.")
            throw OutOfStockException(message)
        }
    }

    private fun addressesAreCompleteCondition() = PreCondition<OrderPlacement> { orderPlacement ->
        if (orderPlacement.addresses.isIncomplete()) {
            val message = ErrorMessage("An order needs a complete delivery address and billing address.")
            throw IncompleteOrderAddressException(message)
        }
    }

    private fun orderIsStoredCondition() = PostCondition<OrderPlacement> { orderPlacement ->
        if (orderRepository.orderIsMissing(orderPlacement.orderId)) {
            throw OrderPlacementFailed(ErrorMessage("Order ${orderPlacement.orderId.value} was not stored."))
        }
    }
}
