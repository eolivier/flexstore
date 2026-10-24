package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.CartRepository
import org.flexstore.domain.valueobject.*
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface JpaCartItemRepository : JpaRepository<CartItemEntity, String>

// The cart stored in the database: an item keeps the product id and the quantity, the product is read from the catalog
@Repository
@Transactional
class PostgresCartRepositoryAdapter(
    private val jpaRepo: JpaCartItemRepository,
    private val productRepo: JpaProductRepository
) : CartRepository {

    @Transactional(readOnly = true)
    override fun load(): Cart = Cart(jpaRepo.findAll(Sort.by("createdAt", "id")).map { it.toDomain() })

    override fun save(cart: Cart): Cart {
        val keptIds = cart.items.filterIsInstance<CartItem>().map { it.itemId.id.value }.toSet()
        jpaRepo.deleteAll(jpaRepo.findAll().filterNot { it.id in keptIds })
        jpaRepo.flush()
        cart.items.forEach { item ->
            when (item) {
                is CartItem -> jpaRepo.findById(item.itemId.id.value).ifPresent {
                    jpaRepo.save(it.copy(quantity = item.quantity.value))
                }
                is DraftItem -> jpaRepo.save(
                    CartItemEntity(
                        UUID.randomUUID().toString(),
                        productRepo.getReferenceById(item.product.productId.id.value),
                        item.quantity.value
                    )
                )
                is NoItem -> Unit
            }
        }
        return load()
    }

    private fun CartItemEntity.toDomain() = CartItem(
        itemId = ItemId(Identity(id)),
        product = product.toDomain(),
        quantity = Quantity(quantity)
    )
}
