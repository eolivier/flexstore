package org.flexstore.infra.spring.adapter.persistence.jpa.repository.invoice

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.seller.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.InvoiceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface JpaInvoiceRepository : JpaRepository<InvoiceEntity, String> {

    fun findByCustomerOrderId(customerOrderId: String): InvoiceEntity?

    // The next rank of the invoices of the year: the invoice counter of the year is created at 1, or
    // incremented. Its row stays locked until the end of the transaction, so that two invoices never take the
    // same rank, and an issue rolled back gives its rank back
    @Query(
        value = """INSERT INTO document_counter (id, kind, year, last_rank) VALUES (:id, 'INVOICE', :year, 1)
                   ON CONFLICT (kind, year)
                   DO UPDATE SET last_rank = document_counter.last_rank + 1, updated_at = now()
                   RETURNING last_rank""",
        nativeQuery = true
    )
    fun nextRank(id: String, year: Int): Int
}
