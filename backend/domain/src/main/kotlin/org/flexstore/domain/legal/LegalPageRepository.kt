package org.flexstore.domain.legal

// The legal pages of the shop: their versions are kept, the last one published of each kind is the current one.
// Each kind has a first version, empty until the administrators write it
interface LegalPageRepository {

    fun current(kind: LegalPageKind): LegalPage

    // Adds a version of the page, which becomes the current one of its kind
    fun save(legalPage: LegalPage)
}
