package org.flexstore.domain.entity

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.usecase.product.ProductNotFoundException
import org.flexstore.domain.valueobject.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

/**
 * Cart rules:
 * - an item always holds the catalog product (the product given with the item is only used for its id)
 * - a product is in the cart at most once: adding it again changes the quantity of its item
 * - an item whose quantity becomes zero or negative is removed
 */
class Cart(private val itemRepository: ItemRepository, private val productRepository: ProductRepository) {

    fun getItems() = itemRepository.getItems()

    fun save(item: Item)  {
        when (item) {
            is DraftItem -> add(item)
            is CartItem -> update(item)
            is NoItem -> throw UnknownItemException("Unknown item : $item")
        }
    }

    fun add(draftItem: DraftItem): CartItem {
        if (!draftItem.quantity.isPositive()) {
            throw InvalidQuantityException(NonEmptyString("Quantity must be positive: ${draftItem.quantity.value}"))
        }
        val product = catalogProduct(draftItem.product.productId)
        itemRepository.findByProductId(product.productId)?.let { existingItem ->
            val updatedItem = existingItem.copy(product = product, quantity = draftItem.quantity)
            itemRepository.update(updatedItem)
            return updatedItem
        }
        return itemRepository.add(draftItem.copy(product = product))
    }

    fun update(cartItem: CartItem) {
        // Only the quantity is taken from the given item; an item already removed has nothing left to update
        val storedItem = itemRepository.findById(cartItem.itemId) as? CartItem ?: return
        if (cartItem.quantity.isPositive()) {
            itemRepository.update(storedItem.copy(quantity = cartItem.quantity))
        } else {
            itemRepository.remove(storedItem)
        }
    }

    fun removeItem(item: Item) = itemRepository.remove(item)

    fun changeQuantity(item: Item, newQuantity: Quantity) {
        modifyQuantity(item) { it.changeQuantity(newQuantity) }
    }

    fun increaseQuantity(item: Item) = modifyQuantity(item) { it.increase() }

    fun decreaseQuantity(item: Item)  = modifyQuantity(item) { it.decrease() }

    fun modifyQuantity(item: Item, operation: (Item) -> Item) {
        when (item) {
            is DraftItem -> add(operation(item) as DraftItem)
            is CartItem -> update(operation(item) as CartItem)
            is NoItem -> throw UnknownItemException("Unknown item : $item")
        }
    }

    fun findBy(itemId: ItemId) = itemRepository.findById(itemId)

    private fun catalogProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(NonEmptyString("Product ${productId.id.value} not found."))
}

data class InvalidQuantityException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
