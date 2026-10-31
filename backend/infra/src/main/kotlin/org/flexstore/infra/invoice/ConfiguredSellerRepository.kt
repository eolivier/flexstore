package org.flexstore.infra.invoice

import org.flexstore.domain.invoice.*

// The seller written on the invoices, given by the configuration of the shop
class ConfiguredSellerRepository(private val seller: Seller) : SellerRepository {

    override fun load() = seller
}
