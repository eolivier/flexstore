package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.ucop.domain.entity.*

// The document of an invoice or of a credit note, to download (a PDF)
class RenderInvoiceUseCase(
    private val invoiceRenderer: InvoiceRenderer
) : QueryUseCase<BillingDocument, InvoicePdf> {

    override fun getPreConditions(): PreConditions<BillingDocument> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<BillingDocument, InvoicePdf> { billingDocument ->
        invoiceRenderer.render(billingDocument)
    }

    override fun getPostConditions(): PostConditions<InvoicePdf> = PostConditions.none()
}
