package org.flexstore.infra.spring.adapter.persistence.jpa.repository.legal

import org.flexstore.domain.legal.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.LegalPageEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The versions of the legal pages, kept in PostgreSQL: the first one of each kind is created by the migrations,
// each saved page takes the next version number of its kind
@Repository
@Transactional
class PostgresLegalPageRepositoryAdapter(private val jpaRepo: JpaLegalPageRepository) : LegalPageRepository {

    @Transactional(readOnly = true)
    override fun current(kind: LegalPageKind): LegalPage =
        jpaRepo.findFirstByKindOrderByVersionDesc(kind.name).toDomain()

    override fun save(legalPage: LegalPage) {
        val nextVersion = jpaRepo.findFirstByKindOrderByVersionDesc(legalPage.kind.name).version + 1
        val legalPageEntity = LegalPageEntity(
            legalPage.id.value.value,
            legalPage.kind.name,
            nextVersion,
            legalPage.text.value,
            legalPage.publishedAt.value
        )
        jpaRepo.save(legalPageEntity)
    }

    private fun LegalPageEntity.toDomain(): LegalPage {
        val legalPageId = LegalPageId(Identity(id))
        return LegalPage(legalPageId, LegalPageKind.valueOf(kind), LegalText(text), PublishedAt(publishedAt))
    }
}
