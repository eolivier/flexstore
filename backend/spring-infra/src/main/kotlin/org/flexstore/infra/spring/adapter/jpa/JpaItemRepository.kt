package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String> {
    fun findByProductId(productId: String): CartItemEntity?
}

// Cart items stored in the database: an item keeps the product id and the quantity, the product is read from the catalog
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
        val product = productRepo.getReferenceById(draftItem.product.productId.id.value)
        val saved = jpaRepo.save(CartItemEntity(UUID.randomUUID().toString(), product, draftItem.quantity.value))
        return CartItem(ItemId(Identity(saved.id)), draftItem.product, draftItem.quantity)
    }

    override fun update(cartItem: CartItem) {
        val existing = jpaRepo.findById(cartItem.itemId.id.value).orElseThrow()
        jpaRepo.save(existing.copy(quantity = cartItem.quantity.value))
    }

    @Transactional(readOnly = true)
    override fun findById(itemId: ItemId): Item =
        jpaRepo.findById(itemId.id.value).map<Item> { it.toDomain() }.orElse(NoItem)

    @Transactional(readOnly = true)
    override fun findByProductId(productId: ProductId): CartItem? = jpaRepo.findByProductId(productId.id.value)?.toDomain()

    override fun clear() = jpaRepo.deleteAll()

    private fun CartItemEntity.toDomain() = CartItem(
        itemId = ItemId(Identity(id)),
        product = product.toDomain(),
        quantity = Quantity(quantity)
    )
}
