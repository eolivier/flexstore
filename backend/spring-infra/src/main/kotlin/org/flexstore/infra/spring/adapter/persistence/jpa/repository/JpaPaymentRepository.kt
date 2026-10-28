package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.PaymentEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.PaymentRefundEntity
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.payment.PaymentProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaPaymentRepository : JpaRepository<PaymentEntity, String>

@Repository
interface JpaPaymentRefundRepository : JpaRepository<PaymentRefundEntity, String> {
    fun existsByPaymentId(paymentId: String): Boolean
}

// The payments: started on the payment provider, kept in the database, referred to by their order
@Repository
@Transactional
class PaymentRepositoryAdapter(
    private val paymentProvider: PaymentProvider,
    private val jpaPaymentRepository: JpaPaymentRepository,
    private val jpaOrderRepository: JpaOrderRepository,
    private val jpaPaymentRefundRepository: JpaPaymentRefundRepository
) : PaymentRepository {

    override fun start(paymentRequest: PaymentRequest) {
        val order = paymentRequest.order
        val providerCheckout = paymentProvider.checkout(paymentRequest)
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

    // An order whose payment never started has nothing to stop
    override fun stop(orderId: OrderId) {
        val paymentId = jpaOrderRepository.findById(orderId.value).orElse(null)?.paymentId ?: return
        paymentProvider.stop(jpaPaymentRepository.findById(paymentId).orElseThrow().providerReference)
    }

    // The refund is kept with its reference on the provider; an order paid without payment (none in practice)
    // has nothing to refund
    override fun refund(orderId: OrderId) {
        val paymentId = jpaOrderRepository.findById(orderId.value).orElse(null)?.paymentId ?: return
        val paymentEntity = jpaPaymentRepository.findById(paymentId).orElseThrow()
        val refundReference = paymentProvider.refund(paymentEntity.providerReference)
        jpaPaymentRefundRepository.save(PaymentRefundEntity(Identity.nextId().value, paymentId, refundReference))
    }

    @Transactional(readOnly = true)
    override fun sessionOf(orderId: OrderId): PaymentSession? {
        val paymentId = jpaOrderRepository.findById(orderId.value).orElse(null)?.paymentId ?: return null
        val paymentEntity = jpaPaymentRepository.findById(paymentId).orElseThrow()
        val checkoutPage = CheckoutPage(paymentEntity.checkoutUrl)
        return PaymentSession(checkoutPage, PaymentOutcome.valueOf(paymentEntity.outcome))
    }
}
