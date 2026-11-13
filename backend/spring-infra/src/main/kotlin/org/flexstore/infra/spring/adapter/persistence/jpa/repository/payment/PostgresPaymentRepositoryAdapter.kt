package org.flexstore.infra.spring.adapter.persistence.jpa.repository.payment

import org.flexstore.domain.order.model.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.PaymentEntity
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.payment.PaymentProvider
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.flexstore.infra.spring.adapter.persistence.jpa.repository.order.JpaOrderRepository

// The payments: started on the payment provider, kept in the database, referred to by their order
@Repository
@Transactional
class PostgresPaymentRepositoryAdapter(
    private val paymentProvider: PaymentProvider,
    private val jpaPaymentRepository: JpaPaymentRepository,
    private val jpaOrderRepository: JpaOrderRepository
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
        val orderEntity = jpaOrderRepository.findById(order.orderId.id.value).orElseThrow()
        val storedCreatedAt = orderEntity.createdAt
        val orderEntityWithPayment = orderEntity.copy(paymentId = storedPayment.id)
        jpaOrderRepository.save(orderEntityWithPayment.also { it.createdAt = storedCreatedAt })
    }

    // An order whose payment never started was not paid
    override fun isCompleted(orderId: OrderId): Boolean {
        val paymentId = jpaOrderRepository.findById(orderId.id.value).orElse(null)?.paymentId ?: return false
        return paymentProvider.isPaid(jpaPaymentRepository.findById(paymentId).orElseThrow().providerReference)
    }

    // An order whose payment never started has nothing to stop
    override fun stop(orderId: OrderId) {
        val paymentId = jpaOrderRepository.findById(orderId.id.value).orElse(null)?.paymentId ?: return
        paymentProvider.stop(jpaPaymentRepository.findById(paymentId).orElseThrow().providerReference)
    }

    // The refund is kept with its reference on the provider, in the payment; an order paid without payment
    // (none in practice) has nothing to refund
    override fun refund(orderId: OrderId) {
        val paymentId = jpaOrderRepository.findById(orderId.id.value).orElse(null)?.paymentId ?: return
        val paymentEntity = jpaPaymentRepository.findById(paymentId).orElseThrow()
        val refundReference = paymentProvider.refund(paymentEntity.providerReference)
        val storedCreatedAt = paymentEntity.createdAt
        val paymentEntityRefunded = paymentEntity.copy(refundProviderReference = refundReference)
        jpaPaymentRepository.save(paymentEntityRefunded.also { it.createdAt = storedCreatedAt })
    }

    @Transactional(readOnly = true)
    override fun sessionOf(orderId: OrderId): PaymentSession? {
        val paymentId = jpaOrderRepository.findById(orderId.id.value).orElse(null)?.paymentId ?: return null
        val paymentEntity = jpaPaymentRepository.findById(paymentId).orElseThrow()
        val checkoutPage = CheckoutPage(paymentEntity.checkoutUrl)
        return PaymentSession(checkoutPage, PaymentOutcome.valueOf(paymentEntity.outcome))
    }
}
