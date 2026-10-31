package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.*
import org.ucop.domain.entity.*

// An order to place, and what it is placed from: the cart of the customer and the threshold of free shipping,
// read before
data class OrderToPlace(val placement: OrderPlacement, val content: OrderContent) {

    val orderId get() = placement.orderId

    val cart get() = content.cart
}

// The order is placed from the cart: it awaits its payment (the cart is emptied once it is paid). The stock
// covers the cart, and the addresses are complete; the units are then reserved (see ReserveOrderStockUseCase)
class PlaceOrderUseCase(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository
) : UseCase<OrderToPlace> {

    override fun getPreConditions(): PreConditions<OrderToPlace> =
        PreConditions(cartHasItemsCondition(), stockCoversTheCartCondition(), addressesAreCompleteCondition())

    override fun getNominalScenario(): NominalScenario<OrderToPlace> {
        val placeOrderStep = Step<OrderToPlace> { orderToPlace ->
            orderRepository.save(Order.place(orderToPlace.placement, orderToPlace.content))
        }
        return NominalScenario(placeOrderStep)
    }

    override fun getPostConditions(): PostConditions<OrderToPlace> = PostConditions(orderIsStoredCondition())

    private fun cartHasItemsCondition() = PreCondition<OrderToPlace> { orderToPlace ->
        if (orderToPlace.cart.isEmpty()) {
            throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
        }
    }

    // The order reserves the units, so that they cannot be sold twice while it is paid
    private fun stockCoversTheCartCondition() = PreCondition<OrderToPlace> { orderToPlace ->
        val missingProducts = orderToPlace.cart.items.fold(emptyList<String>()) { missing, product, quantity ->
            val stock = stockRepository.stockOf(product.productId)
            if (stock.doesNotCover(quantity)) missing + product.name.value else missing
        }
        if (missingProducts.isNotEmpty()) {
            val message = ErrorMessage("Not enough units in stock for: ${missingProducts.joinToString()}.")
            throw OutOfStockException(message)
        }
    }

    private fun addressesAreCompleteCondition() = PreCondition<OrderToPlace> { orderToPlace ->
        if (orderToPlace.placement.addresses.isIncomplete()) {
            val message = ErrorMessage("An order needs a complete delivery address and billing address.")
            throw IncompleteOrderAddressException(message)
        }
    }

    private fun orderIsStoredCondition() = PostCondition<OrderToPlace> { orderToPlace ->
        if (orderRepository.orderIsMissing(orderToPlace.orderId)) {
            throw OrderPlacementFailed(ErrorMessage("Order ${orderToPlace.orderId.value} was not stored."))
        }
    }
}
