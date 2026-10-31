package org.flexstore.domain.seller

// The shop that sells, as written on its invoices: its versions are kept, the last one saved is the current one
interface SellerRepository {

    fun current(): Seller

    fun findBy(sellerId: SellerId): Seller?

    // Adds a version of the seller, which becomes the current one
    fun save(seller: Seller)

    fun sellerIsMissing(sellerId: SellerId) = findBy(sellerId) == null
}
