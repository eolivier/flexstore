package org.flexstore.domain.legal

import java.time.Instant
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.InstantProvider
import org.ucop.domain.entity.Actor
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException

// The legal pages of the shop, written by the administrators: its legal notice, its terms of sale and its
// privacy policy
enum class LegalPageKind { LEGAL_NOTICE, TERMS_OF_SALE, PRIVACY_POLICY }

// One version of a legal page: each publication gives a new one, the ones before are kept
data class LegalPageId(val value: Identity) {
    companion object {
        fun nextId() = LegalPageId(Identity.nextId())
    }
}

// The text of a legal page: plain text, its paragraphs separated by line breaks, 50 000 characters at most.
// Empty until the administrators write it
data class LegalText(val value: String) {

    fun isWritten() = value.isNotBlank()

    fun isBlank() = value.isBlank()

    fun isTooLong() = value.length > MAXIMUM_LENGTH

    companion object {
        private const val MAXIMUM_LENGTH = 50_000
    }
}

data class PublishedAt(val value: Instant) {
    companion object {
        fun now() = PublishedAt(InstantProvider.now())
    }
}

// A legal page in one of its versions: its text, and when it was published
data class LegalPage(
    val id: LegalPageId,
    val kind: LegalPageKind,
    val text: LegalText,
    val publishedAt: PublishedAt
) {

    fun isWritten() = text.isWritten()
}

// An administrator publishes a legal page: the publisher is the authenticated user who asks for it
data class LegalPagePublication(val publisher: Actor, val legalPage: LegalPage)

data class LegalPagePublicationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class InvalidLegalPageException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class LegalPagePublicationFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
