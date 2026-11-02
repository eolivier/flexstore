package org.flexstore.domain.legal

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.InstantProvider
import org.ucop.domain.NominalException
import java.time.Instant

// The legal pages of the shop, written by the administrators: its legal notice and its terms of sale
enum class LegalPageKind { LEGAL_NOTICE, TERMS_OF_SALE }

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

data class InvalidLegalPageException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class LegalPagePublicationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
