package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.ucop.domain.entity.*

// The document of an invoice, to download (a PDF)
class RenderInvoiceUseCase(
    private val invoiceRenderer: InvoiceRenderer
) : QueryUseCase<InvoiceDocument, InvoicePdf> {

    override fun getPreConditions(): PreConditions<InvoiceDocument> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<InvoiceDocument, InvoicePdf> { invoiceDocument ->
        invoiceRenderer.render(invoiceDocument)
    }

    override fun getPostConditions(): PostConditions<InvoicePdf> = PostConditions.none()
}
