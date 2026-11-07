package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.pagecontent.*
import org.ucop.domain.entity.*

// The contents of the shop pages, as read by everyone: the pages of the shop show them in their head
class ReadShopPageContentUseCase(private val shopPageContentRepository: ShopPageContentRepository) :
    QueryUseCase<Unit, ShopPageContents> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() =
        NominalQuery<Unit, ShopPageContents> { shopPageContentRepository.all() }

    override fun getPostConditions(): PostConditions<ShopPageContents> = PostConditions.none()
}
