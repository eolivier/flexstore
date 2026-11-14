package org.flexstore.domain.seller

import org.ucop.domain.ErrorMessage

// The shop that sells, as written on its invoices: its versions are kept, the last one saved is the current one
interface SellerRepository {

    fun current(): Seller

    fun findBy(sellerId: SellerId): Seller?

    // The seller, which a precondition has checked to exist
    fun get(sellerId: SellerId): Seller =
        findBy(sellerId) ?: throw SellerNotFoundException(ErrorMessage("Seller $sellerId not found."))

    // Adds a version of the seller, which becomes the current one
    fun save(seller: Seller)

    fun sellerIsMissing(sellerId: SellerId) = findBy(sellerId) == null
}
