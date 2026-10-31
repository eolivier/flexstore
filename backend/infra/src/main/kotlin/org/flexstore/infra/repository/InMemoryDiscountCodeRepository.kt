package org.flexstore.infra.repository

import org.flexstore.domain.discount.*

// The discount codes in memory, the newest first: for the tests
class InMemoryDiscountCodeRepository : DiscountCodeRepository {

    private val discountCodes = mutableListOf<DiscountCode>()

    override fun save(discountCode: DiscountCode) {
        val index = discountCodes.indexOfFirst { it.code == discountCode.code }
        if (index < 0) discountCodes.add(0, discountCode) else discountCodes[index] = discountCode
    }

    override fun findBy(code: PromoCode): DiscountCode? = discountCodes.firstOrNull { it.code == code }

    override fun findAll() = DiscountCodes(discountCodes.toList())

    override fun delete(code: PromoCode) {
        discountCodes.removeIf { it.code == code }
    }
}
