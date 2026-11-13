package org.flexstore.infra.spring.adapter.persistence.jpa.repository.order

import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.AddressEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderLineEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.OrderStatusChangeEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.cart.*
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.user.JpaUserRepository

// The newest orders first; the id keeps the order of two orders placed at the same moment stable
private val NEWEST_FIRST = Sort.by(Sort.Order.desc("placedAt"), Sort.Order.desc("id"))

@Repository
@Transactional
class PostgresOrderRepositoryAdapter(
    private val jpaRepo: JpaOrderRepository,
    private val jpaUserRepository: JpaUserRepository
) : OrderRepository {

    // An order is placed once, then only its status and its tracking change: the stored rows of its lines are
    // kept, and the statuses it reaches are added to its stored history
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
        val tracking = order.tracking as? OrderTracking.Given
        val orderEntity = copy(
            status = order.status.name,
            trackingCarrier = tracking?.carrier?.name,
            trackingNumber = tracking?.trackingNumber?.value,
            statusChanges = statusChanges + newChangeEntities
        )
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

    @Transactional(readOnly = true)
    override fun findAll(pageRequest: PageRequest) = jpaRepo.findAll(pageableOf(pageRequest)).toOrdersPage()

    @Transactional(readOnly = true)
    override fun findAllBy(customer: UserId.ValidUserId, pageRequest: PageRequest) =
        jpaRepo.findAllByUserId(customer.value, pageableOf(pageRequest)).toOrdersPage()

    private fun pageableOf(pageRequest: PageRequest): Pageable {
        val (number, size) = pageRequest
        return org.springframework.data.domain.PageRequest.of(number.value, size.value, NEWEST_FIRST)
    }

    private fun org.springframework.data.domain.Page<OrderEntity>.toOrdersPage() =
        Page(Orders(content.map { it.toDomain() }), TotalCount(totalElements))

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
            legalPageId = (termsOfSale as? AcceptedTermsOfSale)?.termsOfSaleId?.value?.value,
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
                    vatRate = line.vatRate.name,
                    quantity = line.quantity.value
                )
            }
        )
    }

    // The domain keeps the addresses of an order among the addresses of its customer
    private fun List<AddressEntity>.rowOf(address: PostalAddress): AddressEntity {
        val message = "The address of the order is not an address of its customer"
        return firstOrNull { it.toDomain() == address } ?: throw IllegalStateException(message)
    }

    private fun OrderEntity.toDomain(): Order {
        val orderStamp = OrderStamp(OrderId(id), placedAt)
        val orderAddresses = OrderAddresses(deliveryAddress.toDomain(), billingAddress.toDomain())
        val shippingMethod = ShippingMethod.valueOf(this.shippingMethod)
        val delivery = OrderDelivery(orderAddresses, shippingMethod)
        val termsOfSale = legalPageId
            ?.let { id -> AcceptedTermsOfSale(LegalPageId(Identity(id))) }
            ?: NoTermsOfSaleAccepted
        val checkout = Checkout(CartOwner.Customer(UserId.ValidUserId(userId)), delivery, termsOfSale)
        val orderLines = OrderLines(lines.map { it.toDomain() })
        val currency = orderLines.totalPrice().currency
        val orderShipping = OrderShipping(shippingMethod, Price(Amount(shippingCost), currency))
        val orderDiscount = discountCode
            ?.let { code -> AppliedOrderDiscount(PromoCode(code), Price(Amount(discountAmount), currency)) }
            ?: NoOrderDiscount
        val orderBill = OrderBill(orderLines, OrderAdjustments(orderShipping, orderDiscount))
        val orderDetail = OrderDetail(OrderPlacement(orderStamp, checkout), orderBill)
        val history = OrderStatusHistory(statusChanges.map { it.toDomain() })
        return Order(orderDetail, history, orderTracking())
    }

    // The carrier and the number are given together, or none (a check of the table)
    private fun OrderEntity.orderTracking(): OrderTracking {
        val carrier = trackingCarrier ?: return OrderTracking.None
        val number = trackingNumber ?: return OrderTracking.None
        return OrderTracking.Given(Carrier.valueOf(carrier), TrackingNumber(number))
    }

    private fun OrderStatusChange.toEntity(position: Int) =
        OrderStatusChangeEntity(Identity.nextId().value, position, status.name, changedAt.value)

    private fun OrderStatusChangeEntity.toDomain() =
        OrderStatusChange(OrderStatus.valueOf(status), StatusChangedAt(changedAt))

    private fun OrderLineEntity.toDomain(): OrderLine {
        val orderedProduct = OrderedProduct(ProductId(Identity(productId)), Name(productName))
        val unitPrice = Price(Amount(unitPrice), Currency.valueOf(currency))
        val pricing = ProductPricing(unitPrice, VatRate.valueOf(vatRate))
        return OrderLine(PricedProduct(orderedProduct, pricing), Quantity(quantity))
    }
}
