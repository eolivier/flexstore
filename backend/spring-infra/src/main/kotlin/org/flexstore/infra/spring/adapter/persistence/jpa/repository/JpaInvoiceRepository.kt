package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.InvoiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

@Repository
interface JpaInvoiceRepository : JpaRepository<InvoiceEntity, String> {

    fun findByCustomerOrderId(customerOrderId: String): InvoiceEntity?

    // The next rank of the invoices of the year: the counter of the year is created at 1, or incremented. Its
    // row stays locked until the end of the transaction, so that two invoices never take the same rank, and an
    // issue rolled back gives its rank back
    @Query(
        value = """INSERT INTO invoice_counter (id, year, last_rank) VALUES (:id, :year, 1)
                   ON CONFLICT (year) DO UPDATE SET last_rank = invoice_counter.last_rank + 1, updated_at = now()
                   RETURNING last_rank""",
        nativeQuery = true
    )
    fun nextRank(id: String, year: Int): Int
}

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
            sellerId.value.value
        )
        return jpaRepo.save(invoiceEntity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findBy(orderId: OrderId): Invoice? = jpaRepo.findByCustomerOrderId(orderId.value)?.toDomain()

    private fun InvoiceEntity.toDomain(): Invoice {
        val invoiceIssue = InvoiceIssue(OrderId(customerOrderId), InvoiceDate(issuedAt))
        return Invoice(InvoiceNumber(number), invoiceIssue, SellerId(Identity(sellerId)))
    }
}

private const val RANK_DIGITS = 6
