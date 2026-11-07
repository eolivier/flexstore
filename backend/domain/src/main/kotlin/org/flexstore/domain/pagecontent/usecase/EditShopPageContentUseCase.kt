package org.flexstore.domain.pagecontent.usecase

import org.flexstore.domain.pagecontent.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator edits the content of a type of the shop pages (the key words, the description, the Google
// Analytics identifier): 1 000 characters at most, an identifier written as such. An empty text clears it. It
// is the only way to change a content, whatever its type
class EditShopPageContentUseCase(
    private val shopPageContentRepository: ShopPageContentRepository,
    private val userRepository: UserRepository
) : UseCase<ShopPageContentChange> {

    override fun getPreConditions(): PreConditions<ShopPageContentChange> =
        PreConditions(editorIsAdministratorCondition(), contentIsValidCondition())

    override fun getNominalScenario(): NominalScenario<ShopPageContentChange> =
        NominalScenario(Step { change -> shopPageContentRepository.save(change.content) })

    override fun getPostConditions(): PostConditions<ShopPageContentChange> =
        PostConditions(contentIsSavedCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<ShopPageContentChange> { change ->
        if (userRepository.isNotAdministrator(change.editor)) {
            throw ShopPageContentForbiddenException(
                ErrorMessage("Only an administrator can edit the content of the shop pages.")
            )
        }
    }

    private fun contentIsValidCondition() = PreCondition<ShopPageContentChange> { change ->
        val content = change.content
        val reason = when {
            content.isTooLong() -> "its text is ${ShopPageContentText.MAXIMUM_LENGTH} characters at most"
            content.isMalformed() -> "its text is letters, digits and hyphens only"
            else -> null
        }
        if (reason != null) {
            throw InvalidShopPageContentException(
                ErrorMessage("The content ${content.type} is not valid: $reason.")
            )
        }
    }

    private fun contentIsSavedCondition() = PostCondition<ShopPageContentChange> { change ->
        val content = change.content
        if (shopPageContentRepository.all().of(content.type) != content) {
            throw ShopPageContentChangeFailed(ErrorMessage("The content ${content.type} was not saved."))
        }
    }
}
