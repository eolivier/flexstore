package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.actor.isNotAtLeastVisitor
import org.ucop.domain.ErrorMessage
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
        if (reader.isNotAtLeastVisitor()) {
            val message = ErrorMessage("The reader must be at least a visitor to read the contents of the shop pages.")
            throw ShopPageContentReadingForbiddenException(message)
        }
    }
}
