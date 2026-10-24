package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.model.*
import org.flexstore.domain.sharedkernel.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaCartRepository : JpaRepository<CartEntity, String> {
    fun findByUserId(userId: String): CartEntity?
}

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String> {
    fun findAllByCartId(cartId: String, sort: Sort): List<CartItemEntity>
}

// The carts stored in the database: an item keeps the product id and the quantity, the product is read from the catalog
@Repository
@Transactional
class PostgresCartRepositoryAdapter(
    private val cartRepo: JpaCartRepository,
    private val itemRepo: JpaCartItemRepository,
    private val productRepo: JpaProductRepository
) : CartRepository {

    @Transactional(readOnly = true)
    override fun load(owner: CartOwner): Cart {
        val cartEntity = find(owner) ?: return Cart(owner)
        return Cart(owner, Items(items(cartEntity).map { it.toDomain() }))
    }

    override fun save(cart: Cart): Cart {
        val cartEntity = find(cart.owner) ?: cartRepo.save(newCartEntity(cart.owner))
        val keptIds = cart.items.cartItems().itemIds().map { it.id.value }.toSet()
        itemRepo.deleteAll(items(cartEntity).filterNot { it.id in keptIds })
        itemRepo.flush()
        cart.items.asList().forEach { item ->
            when (item) {
                is CartItem -> itemRepo.findById(item.itemId.id.value).ifPresent {
                    itemRepo.save(it.copy(quantity = item.quantity.value))
                }
                is DraftItem -> itemRepo.save(
                    CartItemEntity(
                        id = Identity.nextId().value,
                        cartId = cartEntity.id,
                        product = productRepo.getReferenceById(item.product.productId.id.value),
                        quantity = item.quantity.value
                    )
                )
                is NoItem -> Unit
            }
        }
        return load(cart.owner)
    }

    override fun delete(owner: CartOwner) {
        find(owner)?.let { cartEntity ->
            itemRepo.deleteAll(items(cartEntity))
            cartRepo.delete(cartEntity)
        }
    }

    private fun find(owner: CartOwner): CartEntity? = when (owner) {
        is CartOwner.Customer -> cartRepo.findByUserId(owner.userId.value)
        // A visitor id only maps to a visitor cart, never to the cart of a customer
        is CartOwner.Visitor -> cartRepo.findById(owner.cartId.value).filter { it.userId == null }.orElse(null)
    }

    private fun newCartEntity(owner: CartOwner) = when (owner) {
        is CartOwner.Customer -> CartEntity(id = Identity.nextId().value, userId = owner.userId.value)
        is CartOwner.Visitor -> CartEntity(id = owner.cartId.value)
    }

    private fun items(cartEntity: CartEntity) = itemRepo.findAllByCartId(cartEntity.id, Sort.by("createdAt", "id"))

    private fun CartItemEntity.toDomain() = CartItem(
        itemId = ItemId(Identity(id)),
        product = product.toDomain(),
        quantity = Quantity(quantity)
    )
}
