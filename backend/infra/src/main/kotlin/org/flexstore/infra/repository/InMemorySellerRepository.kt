package org.flexstore.infra.repository

import org.flexstore.domain.seller.*

// The versions of the seller in memory, from a first one: for the tests
class InMemorySellerRepository(firstSeller: Seller) : SellerRepository {

    private val sellers = mutableListOf(firstSeller)

    override fun current() = sellers.last()

    override fun findBy(sellerId: SellerId) = sellers.firstOrNull { it.id == sellerId }

    override fun save(seller: Seller) {
        sellers.add(seller)
    }
}
