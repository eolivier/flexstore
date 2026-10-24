package org.flexstore.domain.usecase.basket

import org.flexstore.domain.repository.CartRepository
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.usecase.product.ProductNotFoundException
import org.flexstore.domain.valueobject.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*

data class AddItemToCartRequest(val productId: ProductId, val quantity: Quantity)

class AddItemToCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : UseCase<AddItemToCartRequest> {

    override fun getPreConditions(): List<PreCondition<AddItemToCartRequest>> = listOf(productExistsCondition())

    override fun getNominalScenario(): NominalScenario<AddItemToCartRequest> {
        val addItemStep = Step<AddItemToCartRequest> { request ->
            val cart = cartRepository.load()
            cartRepository.save(cart.add(catalogProduct(request.productId), request.quantity))
        }
        return NominalScenario(listOf(addItemStep))
    }

    override fun getPostConditions(): List<PostCondition<AddItemToCartRequest>> = listOf(itemHasQuantityCondition())

    private fun catalogProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(NonEmptyString("Product ${productId.id.value} not found."))

    private fun productExistsCondition() = PreCondition<AddItemToCartRequest> { request ->
        catalogProduct(request.productId)
    }

    private fun itemHasQuantityCondition() = PostCondition<AddItemToCartRequest> { request ->
        val item = cartRepository.load().findBy(request.productId)
        if (item !is CartItem || item.quantity != request.quantity) {
            throw CartUpdateFailed(NonEmptyString("Product ${request.productId.id.value} was not added to the cart."))
        }
    }
}

data class CartUpdateFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
