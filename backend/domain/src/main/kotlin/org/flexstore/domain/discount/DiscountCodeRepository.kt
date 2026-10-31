package org.flexstore.domain.discount

// The discount codes given by the administrators
interface DiscountCodeRepository {

    fun save(discountCode: DiscountCode)

    fun findBy(code: PromoCode): DiscountCode?

    // Newest first
    fun findAll(): DiscountCodes

    fun delete(code: PromoCode)

    fun codeExists(code: PromoCode) = findBy(code) != null

    fun codeIsMissing(code: PromoCode) = !codeExists(code)
}

// Discount codes, in the order given
data class DiscountCodes(private val discountCodes: List<DiscountCode>) {

    constructor(vararg discountCodes: DiscountCode) : this(discountCodes.toList())

    // Read-only view for the adapters (JSON)
    fun asList(): List<DiscountCode> = discountCodes.toList()
}
