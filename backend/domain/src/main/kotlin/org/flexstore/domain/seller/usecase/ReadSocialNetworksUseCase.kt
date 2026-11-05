package org.flexstore.domain.seller.usecase

import org.flexstore.domain.seller.*
import org.ucop.domain.entity.*

// Anyone reads the social networks of the current seller, to find the shop there
class ReadSocialNetworksUseCase(private val sellerRepository: SellerRepository) :
    QueryUseCase<SocialNetworksReading, SocialNetworks> {

    override fun getPreConditions(): PreConditions<SocialNetworksReading> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<SocialNetworksReading, SocialNetworks> {
        sellerRepository.current().detail.socialNetworks
    }

    override fun getPostConditions(): PostConditions<SocialNetworks> = PostConditions.none()
}
