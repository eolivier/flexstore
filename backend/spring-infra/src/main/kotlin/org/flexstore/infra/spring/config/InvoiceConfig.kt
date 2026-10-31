package org.flexstore.infra.spring.config

import org.flexstore.domain.invoice.*
import org.flexstore.infra.invoice.OpenPdfInvoiceRenderer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The invoices: written as PDF documents
@Configuration
open class InvoiceConfig {

    @Bean
    open fun invoiceRenderer(): InvoiceRenderer = OpenPdfInvoiceRenderer()
}
