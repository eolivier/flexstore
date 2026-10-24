package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.usecase.product.ProductNotFoundException
import org.flexstore.domain.valueobject.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.ucop.domain.NonEmptyString
import java.util.UUID

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String> {
    fun findByProductId(productId: String): CartItemEntity?
}

/**
 * Cart items stored in the database. The product of an item is always read from the catalog:
 * the product data sent by the client (name, price...) is ignored.
 */
@Repository
@Transactional
class PostgresItemRepositoryAdapter(
    private val jpaRepo: JpaCartItemRepository,
    private val productRepo: JpaProductRepository
) : ItemRepository {

    @Transactional(readOnly = true)
    override fun getItems(): Items = Items(findAll())

    @Transactional(readOnly = true)
    override fun findAll(): List<Item> = jpaRepo.findAll(Sort.by("createdAt", "id")).map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun contains(newItem: Item): Boolean = newItem is CartItem && jpaRepo.existsById(newItem.itemId.id.value)

    override fun remove(newItem: Item) {
        if (newItem is CartItem) {
            jpaRepo.deleteById(newItem.itemId.id.value)
        }
    }

    override fun add(draftItem: DraftItem): CartItem {
        val productId = draftItem.product.productId.id.value
        // A product already in the cart: its quantity is replaced
        jpaRepo.findByProductId(productId)?.let { existing ->
            val cartItem = CartItem(ItemId(Identity(existing.id)), existing.product.toDomain(), draftItem.quantity)
            update(cartItem)
            return cartItem
        }
        val product = productRepo.findById(productId).orElseThrow {
            ProductNotFoundException(NonEmptyString("Product $productId not found."))
        }
        return jpaRepo.save(CartItemEntity(UUID.randomUUID().toString(), product, draftItem.quantity.value)).toDomain()
    }

    override fun update(cartItem: CartItem) {
        val existing = jpaRepo.findById(cartItem.itemId.id.value).orElse(null) ?: return
        if (cartItem.quantity.isNegative() || cartItem.quantity.isZero()) {
            jpaRepo.delete(existing)
        } else {
            jpaRepo.save(existing.copy(quantity = cartItem.quantity.value))
        }
    }

    @Transactional(readOnly = true)
    override fun findById(itemId: ItemId): Item =
        jpaRepo.findById(itemId.id.value).map<Item> { it.toDomain() }.orElse(NoItem)

    override fun clear() = jpaRepo.deleteAll()

    private fun CartItemEntity.toDomain() = CartItem(
        itemId = ItemId(Identity(id)),
        product = product.toDomain(),
        quantity = Quantity(quantity)
    )
}
