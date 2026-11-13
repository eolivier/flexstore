package org.flexstore.infra.spring.adapter.persistence.jpa.repository.twofactor

import org.flexstore.infra.spring.adapter.persistence.jpa.entity.TwoFactorChallengeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaTwoFactorChallengeRepository : JpaRepository<TwoFactorChallengeEntity, String>
