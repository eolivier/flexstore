package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.actor.verifyCanRead
import org.flexstore.domain.pagecontent.*
import org.ucop.domain.entity.*

// The contents of the shop pages, as read by everyone: the pages of the shop show them in their head
class ReadShopPageContentUseCase(private val shopPageContentRepository: ShopPageContentRepository) :
    QueryUseCase<Actor, ShopPageContents> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(readerCanReadCondition())

    override fun getNominalQuery() =
        NominalQuery<Actor, ShopPageContents> { shopPageContentRepository.all() }

    override fun getPostConditions(): PostConditions<ShopPageContents> = PostConditions.none()

    // The reader, resolved before the use case runs, is a visitor or higher, or the system
    private fun readerCanReadCondition() = PreCondition<Actor> { reader ->
        reader.verifyCanRead("the contents of the shop pages", ::ShopPageContentReadingForbiddenException)
    }
}
