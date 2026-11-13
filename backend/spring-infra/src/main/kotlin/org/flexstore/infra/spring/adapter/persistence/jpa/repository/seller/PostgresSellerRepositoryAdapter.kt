package org.flexstore.infra.spring.adapter.persistence.jpa.repository.seller

import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.SellerEntity
import org.flexstore.infra.spring.config.SELLERS_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The versions of the seller, kept in PostgreSQL: the first one is created by the migrations, each saved seller
// takes the next version number. The reads are cached; a save evicts every entry, since it changes the current
// seller
@Repository
@Transactional
class PostgresSellerRepositoryAdapter(private val jpaRepo: JpaSellerRepository) : SellerRepository {

    @Cacheable(SELLERS_CACHE, key = "'current'")
    @Transactional(readOnly = true)
    override fun current(): Seller = jpaRepo.findFirstByOrderByVersionDesc().toDomain()

    @Cacheable(SELLERS_CACHE, key = "#p0.toString()")
    @Transactional(readOnly = true)
    override fun findBy(sellerId: SellerId): Seller? =
        jpaRepo.findById(sellerId.toString()).orElse(null)?.toDomain()

    @CacheEvict(SELLERS_CACHE, allEntries = true)
    override fun save(seller: Seller) {
        val nextVersion = jpaRepo.findFirstByOrderByVersionDesc().version + 1
        val (contact, identifier, reach, incorporation, socialNetworks) = seller.detail
        val sellerEntity = SellerEntity(
            seller.id.toString(),
            nextVersion,
            contact.name.value,
            contact.address.value,
            identifier.siret.value,
            identifier.vatNumber.value,
            reach.phone.value.ifEmpty { null },
            reach.email.value.ifEmpty { null },
            reach.website.value.ifEmpty { null },
            incorporation.legalForm.value.ifEmpty { null },
            incorporation.shareCapital.value.ifEmpty { null },
            incorporation.registryCity.value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.FACEBOOK).value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.INSTAGRAM).value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.X).value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.TIKTOK).value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.YOUTUBE).value.ifEmpty { null },
            socialNetworks.urlOf(SocialNetwork.TWITCH).value.ifEmpty { null }
        )
        jpaRepo.save(sellerEntity)
    }

    private fun SellerEntity.toDomain(): Seller {
        val contact = SellerContact(SellerName(name), SellerAddress(address))
        val identifier = SellerIdentifier(Siret(siret), SellerVatNumber(vatNumber))
        // None in the versions written before they were asked
        val sellerPhone = SellerPhone(phone.orEmpty())
        val reach = SellerReach(sellerPhone, SellerEmail(email.orEmpty()), SellerWebsite(website.orEmpty()))
        val incorporation = SellerIncorporation(
            SellerLegalForm(legalForm.orEmpty()),
            ShareCapital(shareCapital.orEmpty()),
            RegistryCity(registryCity.orEmpty())
        )
        val socialNetworks = SocialNetworks.of(
            mapOf(
                SocialNetwork.FACEBOOK to SocialNetworkUrl(facebookUrl.orEmpty()),
                SocialNetwork.INSTAGRAM to SocialNetworkUrl(instagramUrl.orEmpty()),
                SocialNetwork.X to SocialNetworkUrl(xUrl.orEmpty()),
                SocialNetwork.TIKTOK to SocialNetworkUrl(tiktokUrl.orEmpty()),
                SocialNetwork.YOUTUBE to SocialNetworkUrl(youtubeUrl.orEmpty()),
                SocialNetwork.TWITCH to SocialNetworkUrl(twitchUrl.orEmpty())
            )
        )
        val sellerDetail = SellerDetail(contact, identifier, reach, incorporation, socialNetworks)
        return Seller(SellerId(Identity(id)), sellerDetail)
    }
}
