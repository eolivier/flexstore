package org.flexstore.infra.spring.config

import org.flexstore.domain.invoice.*
import org.flexstore.infra.invoice.ConfiguredSellerRepository
import org.flexstore.infra.invoice.OpenPdfInvoiceRenderer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The invoices: written as PDF documents, with the seller given by flexstore.seller (FLEXSTORE_SELLER_...)
@Configuration
open class InvoiceConfig(
    @Value("\${flexstore.seller.name}") private val name: String,
    @Value("\${flexstore.seller.address}") private val address: String,
    @Value("\${flexstore.seller.siret}") private val siret: String,
    @Value("\${flexstore.seller.vat-number}") private val vatNumber: String
) {

    @Bean
    open fun invoiceRenderer(): InvoiceRenderer = OpenPdfInvoiceRenderer()

    @Bean
    open fun sellerRepository(): SellerRepository {
        val contact = SellerContact(SellerName(name), SellerAddress(address))
        val identifiers = SellerIdentifiers(Siret(siret), SellerVatNumber(vatNumber))
        return ConfiguredSellerRepository(Seller(contact, identifiers))
    }
}
