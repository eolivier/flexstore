package org.flexstore.domain.legal.usecase

import org.flexstore.domain.legal.*
import org.ucop.domain.entity.Actor

// The use cases of the legal pages: everyone reads the current version of a page, an administrator publishes a
// new one (read back once saved). The versions before are kept
class LegalPageUseCases(
    private val readLegalPageUseCase: ReadLegalPageUseCase,
    private val publishLegalPageUseCase: PublishLegalPageUseCase
) {
    fun legalPage(kind: LegalPageKind) = readLegalPageUseCase.unfold(kind)

    fun publish(publisher: Actor, kind: LegalPageKind, text: LegalText): LegalPage {
        val legalPage = LegalPage(LegalPageId.nextId(), kind, text, PublishedAt.now())
        publishLegalPageUseCase.unfold(LegalPagePublication(publisher, legalPage))
        return readLegalPageUseCase.unfold(kind)
    }
}
