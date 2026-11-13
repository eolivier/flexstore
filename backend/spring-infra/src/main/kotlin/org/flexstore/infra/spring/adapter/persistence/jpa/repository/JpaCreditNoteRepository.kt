package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.InvoiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId

// The credit note is two columns of the invoice table, written by this statement only (the invoice entity does
// not update them). The update date is set here, as Hibernate does not set it on a bulk update
@Repository
interface JpaCreditNoteRepository : JpaRepository<InvoiceEntity, String> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        "UPDATE invoice SET credit_note_number = :number, credit_note_issued_at = :issuedAt, " +
            "updated_at = now() WHERE id = :invoiceId",
        nativeQuery = true
    )
    fun giveCreditNote(invoiceId: String, number: String, issuedAt: Instant): Int

    // The next rank of the credit notes of the year, as for the invoices: the credit note counter of the year
    // is created at 1, or incremented, and its row stays locked until the end of the transaction
    @Query(
        value = """INSERT INTO document_counter (id, kind, year, last_rank) VALUES (:id, 'CREDIT_NOTE', :year, 1)
                   ON CONFLICT (kind, year)
                   DO UPDATE SET last_rank = document_counter.last_rank + 1, updated_at = now()
                   RETURNING last_rank""",
        nativeQuery = true
    )
    fun nextRank(id: String, year: Int): Int
}

// The credit notes, kept in PostgreSQL: numbered AV, the year of issue (in Paris) and the next rank of that
// year. A credit note is stored in the invoice it cancels
@Repository
@Transactional
class PostgresCreditNoteRepositoryAdapter(
    private val jpaRepo: JpaCreditNoteRepository,
    private val jpaInvoiceRepository: JpaInvoiceRepository
) : CreditNoteRepository {

    override fun issue(creditNoteIssue: CreditNoteIssue): CreditNote {
        val invoiceEntity = jpaInvoiceRepository.findByCustomerOrderId(creditNoteIssue.orderId.value)
            ?: throw IllegalStateException("The order ${creditNoteIssue.orderId.value} has no invoice to cancel")
        val year = creditNoteIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val rank = jpaRepo.nextRank(Identity.nextId().value, year)
        val number = "AV-$year-${rank.toString().padStart(RANK_DIGITS, '0')}"
        jpaRepo.giveCreditNote(invoiceEntity.id, number, creditNoteIssue.issuedAt.value)
        return CreditNote(CreditNoteNumber(number), creditNoteIssue)
    }

    @Transactional(readOnly = true)
    override fun findBy(orderId: OrderId): CreditNote? {
        val invoiceEntity = jpaInvoiceRepository.findByCustomerOrderId(orderId.value) ?: return null
        val number = invoiceEntity.creditNoteNumber ?: return null
        val issuedAt = invoiceEntity.creditNoteIssuedAt ?: return null
        return CreditNote(CreditNoteNumber(number), CreditNoteIssue(orderId, CreditNoteDate(issuedAt)))
    }
}

private const val RANK_DIGITS = 6
