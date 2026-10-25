package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaOrderRepository : JpaRepository<OrderEntity, String> {
    fun findAllByUserId(userId: String): List<OrderEntity>
}

@Repository
@Transactional
class PostgresOrderRepositoryAdapter(
    private val jpaRepo: JpaOrderRepository,
    private val jpaUserRepository: JpaUserRepository
) : OrderRepository {

    override fun save(order: Order): Order = jpaRepo.save(order.toEntity()).toDomain()

    @Transactional(readOnly = true)
    override fun findById(orderId: OrderId): Order? = jpaRepo.findById(orderId.value).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun orderExists(orderId: OrderId): Boolean = jpaRepo.existsById(orderId.value)

    @Transactional(readOnly = true)
    override fun findAllBy(customer: UserId.ValidUserId) = Orders(jpaRepo.findAllByUserId(customer.value).map { it.toDomain() })

    private fun Order.toEntity(): OrderEntity {
        val customerEntity = jpaUserRepository.findById(customer.value).orElseThrow()
        val addressesOfTheCustomer = customerEntity.addresses.map { it.address }
        return OrderEntity(
            id = orderId.value,
            userId = customer.value,
            status = status.name,
            placedAt = placedAt,
            deliveryAddress = addressesOfTheCustomer.rowOf(addresses.delivery),
            billingAddress = addressesOfTheCustomer.rowOf(addresses.billing),
            lines = lines.asList().mapIndexed { position, line ->
                OrderLineEntity(
                    id = Identity.nextId().value,
                    position = position,
                    productId = line.productId.id.value,
                    productName = line.productName.value,
                    unitPrice = line.unitPrice.amount.value,
                    currency = line.unitPrice.currency.name,
                    quantity = line.quantity.value
                )
            }
        )
    }

    // The domain keeps the addresses of an order among the addresses of its customer
    private fun List<AddressEntity>.rowOf(address: PostalAddress) =
        firstOrNull { it.toDomain() == address } ?: throw IllegalStateException("The address of the order is not an address of its customer")

    private fun OrderEntity.toDomain(): Order {
        val orderStamp = OrderStamp(OrderId(id), placedAt)
        val orderAddresses = OrderAddresses(deliveryAddress.toDomain(), billingAddress.toDomain())
        val checkout = Checkout(CartOwner.Customer(UserId.ValidUserId(userId)), orderAddresses)
        val orderLines = OrderLines(lines.map {
            OrderLine(
                productId = ProductId(Identity(it.productId)),
                productName = Name(it.productName),
                unitPrice = Price(Amount(it.unitPrice), Currency.valueOf(it.currency)),
                quantity = Quantity(it.quantity)
            )
        })
        val orderDetail = OrderDetail(OrderPlacement(orderStamp, checkout), orderLines)
        return Order(orderDetail, OrderStatus.valueOf(status))
    }
}
