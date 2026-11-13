package org.flexstore.infra.spring.adapter.persistence.jpa.repository.invoice

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.sharedkernel.Identity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId

// The credit notes, kept in PostgreSQL: numbered AV, the year of issue (in Paris) and the next rank of that
// year. A credit note is stored in the invoice it cancels
@Repository
@Transactional
class PostgresCreditNoteRepositoryAdapter(
    private val jpaRepo: JpaCreditNoteRepository,
    private val jpaInvoiceRepository: JpaInvoiceRepository
) : CreditNoteRepository {

    override fun issue(creditNoteIssue: CreditNoteIssue): CreditNote {
        val invoiceEntity = jpaInvoiceRepository.findByCustomerOrderId(creditNoteIssue.orderId.id.value)
            ?: throw IllegalStateException("The order ${creditNoteIssue.orderId} has no invoice to cancel")
        val year = creditNoteIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val rank = jpaRepo.nextRank(Identity.nextId().value, year)
        val number = "AV-$year-${rank.toString().padStart(RANK_DIGITS, '0')}"
        jpaRepo.giveCreditNote(invoiceEntity.id, number, creditNoteIssue.issuedAt.value)
        return CreditNote(CreditNoteNumber(number), creditNoteIssue)
    }

    @Transactional(readOnly = true)
    override fun findBy(orderId: OrderId): CreditNote? {
        val invoiceEntity = jpaInvoiceRepository.findByCustomerOrderId(orderId.id.value) ?: return null
        val number = invoiceEntity.creditNoteNumber ?: return null
        val issuedAt = invoiceEntity.creditNoteIssuedAt ?: return null
        return CreditNote(CreditNoteNumber(number), CreditNoteIssue(orderId, CreditNoteDate(issuedAt)))
    }
}

private const val RANK_DIGITS = 6
