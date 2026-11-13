package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.pagecontent.*
import org.ucop.domain.entity.Actor

// The use cases of the contents of the shop pages: everyone reads them, an administrator edits one (read back
// once saved)
class ShopPageContentUseCases(
    private val readShopPageContentUseCase: ReadShopPageContentUseCase,
    private val editShopPageContentUseCase: EditShopPageContentUseCase
) {
    fun contents(reader: Actor) = readShopPageContentUseCase.unfold(reader)

    fun edit(editor: Actor, content: ShopPageContent): ShopPageContents {
        editShopPageContentUseCase.unfold(ShopPageContentChange(editor, content))
        return contents(editor)
    }
}
