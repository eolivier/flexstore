package org.flexstore.domain.seller.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
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
        if (reading.reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("Only a visitor, a user or the system can read the social networks of the seller.")
            throw SellerReadingForbiddenException(message)
        }
    }
}
