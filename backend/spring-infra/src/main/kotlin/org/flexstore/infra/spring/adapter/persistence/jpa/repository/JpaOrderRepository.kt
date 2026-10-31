package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.AddressEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderLineEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderStatusChangeEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.cart.*
import org.flexstore.domain.discount.PromoCode
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

    // An order is placed once, then only its status changes: the stored rows of its lines are kept, and the
    // statuses it reaches are added to its stored history
    override fun save(order: Order): Order {
        val storedOrder = jpaRepo.findById(order.orderId.value).orElse(null)
        val orderEntity = storedOrder?.withStatusOf(order) ?: order.toEntity()
        return jpaRepo.save(orderEntity).toDomain()
    }

    private fun OrderEntity.withStatusOf(order: Order): OrderEntity {
        val storedCreatedAt = createdAt
        val storedCount = statusChanges.size
        val newChanges = order.history.asList().drop(storedCount)
        val newChangeEntities = newChanges.mapIndexed { index, change -> change.toEntity(storedCount + index) }
        val orderEntity = copy(status = order.status.name, statusChanges = statusChanges + newChangeEntities)
        return orderEntity.also { it.createdAt = storedCreatedAt }
    }

    @Transactional(readOnly = true)
    override fun findById(orderId: OrderId): Order? =
        jpaRepo.findById(orderId.value).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun orderExists(orderId: OrderId): Boolean = jpaRepo.existsById(orderId.value)

    @Transactional(readOnly = true)
    override fun findAll() = Orders(jpaRepo.findAll().map { it.toDomain() })

    @Transactional(readOnly = true)
    override fun findAllBy(customer: UserId.ValidUserId) =
        Orders(jpaRepo.findAllByUserId(customer.value).map { it.toDomain() })

    private fun Order.toEntity(): OrderEntity {
        val customerEntity = jpaUserRepository.findById(customer.value).orElseThrow()
        val addressesOfTheCustomer = customerEntity.addresses.map { it.address }
        return OrderEntity(
            id = orderId.value,
            userId = customer.value,
            status = status.name,
            placedAt = placedAt,
            shippingMethod = shipping.method.name,
            shippingCost = shipping.cost.amount.value,
            discountCode = (discount as? AppliedOrderDiscount)?.code?.value,
            discountAmount = discountAmount().amount.value,
            deliveryAddress = addressesOfTheCustomer.rowOf(addresses.delivery),
            billingAddress = addressesOfTheCustomer.rowOf(addresses.billing),
            statusChanges = history.asList().mapIndexed { position, change -> change.toEntity(position) },
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
        val shippingMethod = ShippingMethod.valueOf(this.shippingMethod)
        val delivery = OrderDelivery(orderAddresses, shippingMethod)
        val checkout = Checkout(CartOwner.Customer(UserId.ValidUserId(userId)), delivery)
        val orderLines = OrderLines(lines.map { it.toDomain() })
        val currency = orderLines.totalPrice().currency
        val orderShipping = OrderShipping(shippingMethod, Price(Amount(shippingCost), currency))
        val orderDiscount = discountCode
            ?.let { code -> AppliedOrderDiscount(PromoCode(code), Price(Amount(discountAmount), currency)) }
            ?: NoOrderDiscount
        val orderBill = OrderBill(orderLines, OrderAdjustments(orderShipping, orderDiscount))
        val orderDetail = OrderDetail(OrderPlacement(orderStamp, checkout), orderBill)
        val history = OrderStatusHistory(statusChanges.map { it.toDomain() })
        return Order(orderDetail, history)
    }

    private fun OrderStatusChange.toEntity(position: Int) =
        OrderStatusChangeEntity(Identity.nextId().value, position, status.name, changedAt.value)

    private fun OrderStatusChangeEntity.toDomain() =
        OrderStatusChange(OrderStatus.valueOf(status), StatusChangedAt(changedAt))

    private fun OrderLineEntity.toDomain(): OrderLine {
        val orderedProduct = OrderedProduct(ProductId(Identity(productId)), Name(productName))
        val unitPrice = Price(Amount(unitPrice), Currency.valueOf(currency))
        return OrderLine(PricedProduct(orderedProduct, unitPrice), Quantity(quantity))
    }
}
