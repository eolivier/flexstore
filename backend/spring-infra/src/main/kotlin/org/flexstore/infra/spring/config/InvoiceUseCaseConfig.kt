package org.flexstore.infra.spring.config

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.user.repository.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the invoice sub-domain, declared step by step: use cases, then their group
@Configuration
open class InvoiceUseCaseConfig {

    @Bean
    open fun issueInvoiceUseCase(orderRepository: OrderRepository,
                                 invoiceRepository: InvoiceRepository,
                                 sellerRepository: SellerRepository) =
        IssueInvoiceUseCase(orderRepository, invoiceRepository, sellerRepository)

    @Bean
    open fun readInvoiceUseCase(invoiceRepository: InvoiceRepository) = ReadInvoiceUseCase(invoiceRepository)

    @Bean
    open fun issueCreditNoteUseCase(orderRepository: OrderRepository,
                                    invoiceRepository: InvoiceRepository,
                                    creditNoteRepository: CreditNoteRepository) =
        IssueCreditNoteUseCase(orderRepository, invoiceRepository, creditNoteRepository)

    @Bean
    open fun readCreditNoteUseCase(creditNoteRepository: CreditNoteRepository) =
        ReadCreditNoteUseCase(creditNoteRepository)

    @Bean
    open fun renderInvoiceUseCase(invoiceRenderer: InvoiceRenderer) = RenderInvoiceUseCase(invoiceRenderer)

    @Bean
    open fun invoiceUseCases(readInvoiceUseCase: ReadInvoiceUseCase,
                             readCreditNoteUseCase: ReadCreditNoteUseCase,
                             readSellerUseCase: ReadSellerUseCase,
                             renderInvoiceUseCase: RenderInvoiceUseCase) =
        InvoiceUseCases(readInvoiceUseCase, readCreditNoteUseCase, readSellerUseCase, renderInvoiceUseCase)
}
