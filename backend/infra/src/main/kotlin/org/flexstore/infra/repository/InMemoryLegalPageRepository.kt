package org.flexstore.infra.repository

import org.flexstore.domain.legal.*

// The versions of the legal pages in memory, from a first one of each kind: for the tests
class InMemoryLegalPageRepository(firstLegalPages: List<LegalPage>) : LegalPageRepository {

    private val legalPages = firstLegalPages.toMutableList()

    override fun current(kind: LegalPageKind) = legalPages.last { it.kind == kind }

    override fun save(legalPage: LegalPage) {
        legalPages.add(legalPage)
    }

    fun findBy(legalPageId: LegalPageId) = legalPages.firstOrNull { it.id == legalPageId }
}
