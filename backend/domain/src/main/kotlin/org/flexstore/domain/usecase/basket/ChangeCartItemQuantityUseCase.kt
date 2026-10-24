package org.flexstore.domain.usecase.basket

import org.flexstore.domain.repository.CartRepository
import org.flexstore.domain.valueobject.ItemId
import org.flexstore.domain.valueobject.Quantity
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*

// A quantity of zero or less removes the item
data class ChangeCartItemQuantityRequest(val itemId: ItemId, val quantity: Quantity)

class ChangeCartItemQuantityUseCase(private val cartRepository: CartRepository) : UseCase<ChangeCartItemQuantityRequest> {

    override fun getPreConditions(): List<PreCondition<ChangeCartItemQuantityRequest>> = listOf(EmptyPreCondition())

    override fun getNominalScenario(): NominalScenario<ChangeCartItemQuantityRequest> {
        val changeQuantityStep = Step<ChangeCartItemQuantityRequest> { request ->
            val cart = cartRepository.load()
            cartRepository.save(cart.changeQuantity(request.itemId, request.quantity))
        }
        return NominalScenario(listOf(changeQuantityStep))
    }

    override fun getPostConditions(): List<PostCondition<ChangeCartItemQuantityRequest>> = listOf(quantityChangedCondition())

    private fun quantityChangedCondition() = PostCondition<ChangeCartItemQuantityRequest> { request ->
        val item = cartRepository.load().findBy(request.itemId)
        val changed = if (request.quantity.isPositive()) item == null || item.quantity == request.quantity else item == null
        if (!changed) {
            throw CartUpdateFailed(NonEmptyString("Quantity of item ${request.itemId.id.value} was not changed."))
        }
    }
}
