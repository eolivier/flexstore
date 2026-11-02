package org.flexstore.infra.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.OrderId
import java.time.ZoneId

// The credit notes in memory, numbered from 1 in the order they are issued: for the tests
class InMemoryCreditNoteRepository : CreditNoteRepository {

    private val creditNotes = mutableListOf<CreditNote>()

    override fun issue(creditNoteIssue: CreditNoteIssue): CreditNote {
        val year = creditNoteIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val number = CreditNoteNumber("AV-$year-${(creditNotes.size + 1).toString().padStart(6, '0')}")
        val creditNote = CreditNote(number, creditNoteIssue)
        creditNotes.add(creditNote)
        return creditNote
    }

    override fun findBy(orderId: OrderId): CreditNote? = creditNotes.firstOrNull { it.orderId == orderId }
}
