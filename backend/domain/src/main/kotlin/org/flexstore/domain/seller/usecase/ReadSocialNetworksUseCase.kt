package org.flexstore.domain.seller.usecase

import org.flexstore.domain.actor.verifyCanRead
import org.flexstore.domain.seller.*
import org.ucop.domain.entity.*

// Anyone reads the social networks of the current seller, to find the shop there
class ReadSocialNetworksUseCase(private val sellerRepository: SellerRepository) :
    QueryUseCase<SocialNetworksReading, SocialNetworks> {

    override fun getPreConditions(): PreConditions<SocialNetworksReading> =
        PreConditions(readerCanReadCondition())

    override fun getNominalQuery() = NominalQuery<SocialNetworksReading, SocialNetworks> {
        sellerRepository.current().detail.socialNetworks
    }

    override fun getPostConditions(): PostConditions<SocialNetworks> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<SocialNetworksReading> { reading ->
        reading.reader.verifyCanRead("the social networks of the seller", ::SellerReadingForbiddenException)
    }
}
