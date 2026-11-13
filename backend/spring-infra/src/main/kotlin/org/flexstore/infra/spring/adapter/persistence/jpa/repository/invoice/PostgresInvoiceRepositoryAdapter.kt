package org.flexstore.infra.spring.adapter.persistence.jpa.repository.invoice

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.InvoiceEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

// The invoices, kept in PostgreSQL: numbered with the year of issue (in Paris) and the next rank of that year
@Repository
@Transactional
class PostgresInvoiceRepositoryAdapter(private val jpaRepo: JpaInvoiceRepository) : InvoiceRepository {

    override fun issue(invoiceIssue: InvoiceIssue, sellerId: SellerId): Invoice {
        val year = invoiceIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val rank = jpaRepo.nextRank(Identity.nextId().value, year)
        val number = "$year-${rank.toString().padStart(RANK_DIGITS, '0')}"
        val invoiceEntity = InvoiceEntity(
            Identity.nextId().value,
            number,
            invoiceIssue.orderId.value,
            invoiceIssue.issuedAt.value,
            sellerId.value
        )
        return jpaRepo.save(invoiceEntity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findBy(orderId: OrderId): Invoice? = jpaRepo.findByCustomerOrderId(orderId.value)?.toDomain()

    private fun InvoiceEntity.toDomain(): Invoice {
        val invoiceIssue = InvoiceIssue(OrderId(customerOrderId), InvoiceDate(issuedAt))
        return Invoice(InvoiceNumber(number), invoiceIssue, SellerId(sellerId))
    }
}

private const val RANK_DIGITS = 6
