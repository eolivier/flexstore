package org.flexstore.domain.discount

import org.ucop.domain.ErrorMessage

// The discount codes given by the administrators
interface DiscountCodeRepository {

    fun save(discountCode: DiscountCode)

    fun findBy(code: PromoCode): DiscountCode?

    // The discount code, which a precondition has checked to exist
    fun get(code: PromoCode): DiscountCode =
        findBy(code) ?: throw DiscountCodeNotFoundException(ErrorMessage("Discount code $code not found."))

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
