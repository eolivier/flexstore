package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.payment.PaymentProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaPaymentRepository : JpaRepository<PaymentEntity, String>

// The payments: started on the payment provider, kept in the database, referred to by their order
@Repository
@Transactional
class PaymentRepositoryAdapter(
    private val paymentProvider: PaymentProvider,
    private val jpaPaymentRepository: JpaPaymentRepository,
    private val jpaOrderRepository: JpaOrderRepository
) : PaymentRepository {

    override fun start(order: Order) {
        val providerCheckout = paymentProvider.checkout(order)
        val paymentEntity = PaymentEntity(
            Identity.nextId().value,
            providerReference = providerCheckout.reference,
            checkoutUrl = providerCheckout.checkoutPage.url,
            outcome = providerCheckout.outcome.name
        )
        val storedPayment = jpaPaymentRepository.save(paymentEntity)
        val orderEntity = jpaOrderRepository.findById(order.orderId.value).orElseThrow()
        val storedCreatedAt = orderEntity.createdAt
        val orderEntityWithPayment = orderEntity.copy(paymentId = storedPayment.id)
        jpaOrderRepository.save(orderEntityWithPayment.also { it.createdAt = storedCreatedAt })
    }

    @Transactional(readOnly = true)
    override fun sessionOf(orderId: OrderId): PaymentSession? {
        val paymentId = jpaOrderRepository.findById(orderId.value).orElse(null)?.paymentId ?: return null
        val paymentEntity = jpaPaymentRepository.findById(paymentId).orElseThrow()
        val checkoutPage = CheckoutPage(paymentEntity.checkoutUrl)
        return PaymentSession(checkoutPage, PaymentOutcome.valueOf(paymentEntity.outcome))
    }
}
