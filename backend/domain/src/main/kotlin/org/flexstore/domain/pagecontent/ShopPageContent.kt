package org.flexstore.domain.pagecontent

import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

// What the shop says about itself in its pages, written by the administrators: one content for each type
enum class ShopPageContentType { META_KEY_WORDS, GOOGLE_ANALYTICS_ID, META_SHOP_DESCRIPTION }

// The text of a content: plain text, 1 000 characters at most. Empty until the administrators write it
data class ShopPageContentText(val value: String) {

    fun isWritten() = value.isNotBlank()

    fun isTooLong() = value.length > MAXIMUM_LENGTH

    // An identifier of Google Analytics (G-XXXXXXXXXX): letters, digits and hyphens, so that it can be written
    // in a script address without any other meaning
    fun isIdentifier() = IDENTIFIER.matches(value)

    companion object {
        const val MAXIMUM_LENGTH = 1_000
        private val IDENTIFIER = Regex("[A-Za-z0-9-]{1,50}")

        fun empty() = ShopPageContentText("")
    }
}

// The content of one type: its text, empty while the type is not filled in
data class ShopPageContent(val type: ShopPageContentType, val text: ShopPageContentText) {

    fun isTooLong() = text.isTooLong()

    // The identifier of Google Analytics is the only text with a format; an empty text turns it off
    fun isMalformed() =
        type == ShopPageContentType.GOOGLE_ANALYTICS_ID && text.isWritten() && text.isIdentifier().not()
}

// The contents of the shop pages, one for each type
data class ShopPageContents(private val contents: List<ShopPageContent>) {

    constructor(vararg contents: ShopPageContent) : this(contents.toList())

    // The content of a type, empty when it was never written
    fun of(type: ShopPageContentType) =
        contents.firstOrNull { it.type == type } ?: ShopPageContent(type, ShopPageContentText.empty())

    fun with(content: ShopPageContent) = ShopPageContents(contents.filter { it.type != content.type } + content)

    // For the adapters, which map the contents to JSON
    fun asList() = contents
}

// An administrator changes the content of a type: the editor is the authenticated user who asks for it
data class ShopPageContentChange(val editor: UserId, val content: ShopPageContent)

data class ShopPageContentForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidShopPageContentException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class ShopPageContentChangeFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
