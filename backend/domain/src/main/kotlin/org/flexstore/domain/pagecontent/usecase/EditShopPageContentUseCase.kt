package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.pagecontent.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator edits the content of a type of the shop pages (the key words, the description, the Google
// Analytics identifier): 1 000 characters at most, an identifier written as such. An empty text clears it. It
// is the only way to change a content, whatever its type
class EditShopPageContentUseCase(
    private val shopPageContentRepository: ShopPageContentRepository
) : UseCase<ShopPageContentChange> {

    override fun getPreConditions(): PreConditions<ShopPageContentChange> =
        PreConditions(editorIsAdministratorCondition(), contentIsValidCondition())

    override fun getNominalScenario(): NominalScenario<ShopPageContentChange> =
        NominalScenario(Step { change -> shopPageContentRepository.save(change.content) })

    override fun getPostConditions(): PostConditions<ShopPageContentChange> =
        PostConditions(contentIsSavedCondition())

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<ShopPageContentChange> { change ->
        if (change.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can edit the content of the shop pages.")
            throw ShopPageContentForbiddenException(message)
        }
    }

    private fun contentIsValidCondition() = PreCondition<ShopPageContentChange> { change ->
        val content = change.content
        if (content.isTooLong()) {
            throw invalidContent(content, "its text is ${ShopPageContentText.MAXIMUM_LENGTH} characters at most")
        }
        if (content.isMalformed()) throw invalidContent(content, "its text is letters, digits and hyphens only")
    }

    private fun invalidContent(content: ShopPageContent, reason: String) =
        InvalidShopPageContentException(ErrorMessage("The content ${content.type} is not valid: $reason."))

    private fun contentIsSavedCondition() = PostCondition<ShopPageContentChange> { change ->
        val content = change.content
        if (shopPageContentRepository.all().of(content.type) != content) {
            throw ShopPageContentChangeFailed(ErrorMessage("The content ${content.type} was not saved."))
        }
    }
}
