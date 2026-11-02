package org.flexstore.domain.legal.usecase

import org.flexstore.domain.legal.*
import org.ucop.domain.entity.*

// The current version of a legal page, as read by everyone
class ReadLegalPageUseCase(private val legalPageRepository: LegalPageRepository) :
    QueryUseCase<LegalPageKind, LegalPage> {

    override fun getPreConditions(): PreConditions<LegalPageKind> = PreConditions.none()

    override fun getNominalQuery() =
        NominalQuery<LegalPageKind, LegalPage> { kind -> legalPageRepository.current(kind) }

    override fun getPostConditions(): PostConditions<LegalPage> = PostConditions.none()
}
