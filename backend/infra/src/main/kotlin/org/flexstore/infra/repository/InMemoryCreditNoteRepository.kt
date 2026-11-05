package org.flexstore.infra.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.model.OrderId
import java.time.ZoneId

private const val NUMBER_DIGITS = 6

// The credit notes in memory, numbered from 1 in the order they are issued: for the tests
class InMemoryCreditNoteRepository : CreditNoteRepository {

    private val creditNotes = mutableListOf<CreditNote>()

    override fun issue(creditNoteIssue: CreditNoteIssue): CreditNote {
        val year = creditNoteIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val rank = (creditNotes.size + 1).toString().padStart(NUMBER_DIGITS, '0')
        val number = CreditNoteNumber("AV-$year-$rank")
        val creditNote = CreditNote(number, creditNoteIssue)
        creditNotes.add(creditNote)
        return creditNote
    }

    override fun findBy(orderId: OrderId): CreditNote? = creditNotes.firstOrNull { it.orderId == orderId }
}
