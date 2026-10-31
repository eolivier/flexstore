package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.discount.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.DiscountCodeEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaDiscountCodeRepository : JpaRepository<DiscountCodeEntity, String> {
    fun findByCode(code: String): DiscountCodeEntity?
}

// The discount codes, kept in PostgreSQL; the row of a code keeps its id and its creation date when it is saved
// again (a use more)
@Repository
@Transactional
class PostgresDiscountCodeRepositoryAdapter(
    private val jpaRepo: JpaDiscountCodeRepository
) : DiscountCodeRepository {

    override fun save(discountCode: DiscountCode) {
        val stored = jpaRepo.findByCode(discountCode.code.value)
        val entity = discountCode.toEntity(stored?.id ?: Identity.nextId().value)
        stored?.let { entity.createdAt = it.createdAt }
        jpaRepo.save(entity)
    }

    @Transactional(readOnly = true)
    override fun findBy(code: PromoCode): DiscountCode? = jpaRepo.findByCode(code.value)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAll() = DiscountCodes(jpaRepo.findAll(NEWEST_FIRST).map { it.toDomain() })

    override fun delete(code: PromoCode) {
        jpaRepo.findByCode(code.value)?.let { jpaRepo.delete(it) }
    }

    private fun DiscountCode.toEntity(id: String): DiscountCodeEntity {
        val (reductionType, reductionValue) = when (val reduction = reduction) {
            is PercentageOff -> PERCENTAGE to reduction.percentage.value
            is AmountOff -> AMOUNT to reduction.amount.amount.value
        }
        val validity = offer.validity
        return DiscountCodeEntity(
            id, code.value, reductionType, reductionValue,
            validity.expiresAt.value, validity.usageLimit.value, uses.value
        )
    }

    private fun DiscountCodeEntity.toDomain(): DiscountCode {
        val reduction = when (reductionType) {
            PERCENTAGE -> PercentageOff(Percentage(reductionValue))
            else -> AmountOff(Price(Amount(reductionValue), Currency.EUR))
        }
        val validity = DiscountValidity(ExpiryDate(expiresAt), UsageLimit(usageLimit))
        return DiscountCode(PromoCode(code), DiscountOffer(reduction, validity), UseCount(timesUsed))
    }
}

private const val PERCENTAGE = "PERCENTAGE"
private const val AMOUNT = "AMOUNT"
private val NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt")
