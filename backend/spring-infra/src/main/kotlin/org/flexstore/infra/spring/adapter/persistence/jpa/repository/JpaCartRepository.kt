package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CartEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.CartItemEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaCartRepository : JpaRepository<CartEntity, String> {
    fun findByUserId(userId: String): CartEntity?

    @Query(
        "select c from CartEntity c where c.userId is not null and c.remindedAt is null " +
            "and c.lastChangedAt <= :limit and exists (select i.id from CartItemEntity i where i.cartId = c.id) " +
            "order by c.lastChangedAt"
    )
    fun findAbandonedBefore(@Param("limit") limit: Instant): List<CartEntity>

    // The cart changed: it is not reminded any more, until it is left again
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CartEntity c set c.lastChangedAt = :changedAt, c.remindedAt = null where c.id = :id")
    fun markChanged(@Param("id") id: String, @Param("changedAt") changedAt: Instant)

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CartEntity c set c.remindedAt = :remindedAt where c.userId = :userId")
    fun markReminded(@Param("userId") userId: String, @Param("remindedAt") remindedAt: Instant)
}

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String> {
    fun findAllByCartId(cartId: String, sort: Sort): List<CartItemEntity>
}

// The carts stored in the database: an item keeps the product id and the quantity, the product is read from the
// catalog
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
        cartRepo.markChanged(cartEntity.id, InstantProvider.now())
        return load(cart.owner)
    }

    override fun delete(owner: CartOwner) {
        find(owner)?.let { cartEntity ->
            itemRepo.deleteAll(items(cartEntity))
            cartRepo.delete(cartEntity)
        }
    }

    @Transactional(readOnly = true)
    override fun abandonedCartsAt(limit: AbandonmentLimit): AbandonedCarts {
        val abandonedCarts = cartRepo.findAbandonedBefore(limit.value).map { cartEntity ->
            val customer = CartOwner.Customer(UserId.ValidUserId(cartEntity.userId!!))
            Cart(customer, Items(items(cartEntity).map { it.toDomain() }))
        }
        return AbandonedCarts(abandonedCarts)
    }

    override fun markReminded(customer: CartOwner.Customer) {
        cartRepo.markReminded(customer.userId.value, InstantProvider.now())
    }

    @Transactional(readOnly = true)
    override fun isReminded(customer: CartOwner.Customer) =
        cartRepo.findByUserId(customer.userId.value)?.remindedAt != null

    private fun find(owner: CartOwner): CartEntity? = when (owner) {
        is CartOwner.Customer -> cartRepo.findByUserId(owner.userId.value)
        // A visitor id only maps to a visitor cart, never to the cart of a customer
        is CartOwner.Visitor -> cartRepo.findById(owner.cartId.value).filter { it.userId == null }.orElse(null)
    }

    private fun newCartEntity(owner: CartOwner) = when (owner) {
        is CartOwner.Customer -> CartEntity(id = Identity.nextId().value, userId = owner.userId.value)
        is CartOwner.Visitor -> CartEntity(id = owner.cartId.value)
    }

    private fun items(cartEntity: CartEntity) =
        itemRepo.findAllByCartId(cartEntity.id, Sort.by("createdAt", "id"))

    private fun CartItemEntity.toDomain() = CartItem(
        itemId = ItemId(Identity(id)),
        product = product.toDomain(),
        quantity = Quantity(quantity)
    )
}
