package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SellerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface JpaSellerRepository : JpaRepository<SellerEntity, String> {

    fun findFirstByOrderByVersionDesc(): SellerEntity
}

// The versions of the seller, kept in PostgreSQL: the first one is created by the migrations, each saved seller
// takes the next version number
@Repository
@Transactional
class PostgresSellerRepositoryAdapter(private val jpaRepo: JpaSellerRepository) : SellerRepository {

    @Transactional(readOnly = true)
    override fun current(): Seller = jpaRepo.findFirstByOrderByVersionDesc().toDomain()

    @Transactional(readOnly = true)
    override fun findBy(sellerId: SellerId): Seller? =
        jpaRepo.findById(sellerId.value.value).orElse(null)?.toDomain()

    override fun save(seller: Seller) {
        val nextVersion = jpaRepo.findFirstByOrderByVersionDesc().version + 1
        val (contact, identifiers) = seller.details
        val sellerEntity = SellerEntity(
            seller.id.value.value,
            nextVersion,
            contact.name.value,
            contact.address.value,
            identifiers.siret.value,
            identifiers.vatNumber.value
        )
        jpaRepo.save(sellerEntity)
    }

    private fun SellerEntity.toDomain(): Seller {
        val contact = SellerContact(SellerName(name), SellerAddress(address))
        val identifiers = SellerIdentifiers(Siret(siret), SellerVatNumber(vatNumber))
        return Seller(SellerId(Identity(id)), SellerDetails(contact, identifiers))
    }
}
