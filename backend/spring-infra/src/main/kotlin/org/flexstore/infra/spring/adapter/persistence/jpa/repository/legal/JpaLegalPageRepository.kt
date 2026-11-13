package org.flexstore.infra.spring.adapter.persistence.jpa.repository.legal

import org.flexstore.domain.legal.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.LegalPageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaLegalPageRepository : JpaRepository<LegalPageEntity, String> {

    fun findFirstByKindOrderByVersionDesc(kind: String): LegalPageEntity
}
