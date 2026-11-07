package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.pagecontent.*
import org.flexstore.domain.user.model.UserId

// The use cases of the contents of the shop pages: everyone reads them, an administrator edits one (read back
// once saved)
class ShopPageContentUseCases(
    private val readShopPageContentUseCase: ReadShopPageContentUseCase,
    private val editShopPageContentUseCase: EditShopPageContentUseCase
) {
    fun contents() = readShopPageContentUseCase.unfold(Unit)

    fun edit(editor: UserId, content: ShopPageContent): ShopPageContents {
        editShopPageContentUseCase.unfold(ShopPageContentChange(editor, content))
        return contents()
    }
}
