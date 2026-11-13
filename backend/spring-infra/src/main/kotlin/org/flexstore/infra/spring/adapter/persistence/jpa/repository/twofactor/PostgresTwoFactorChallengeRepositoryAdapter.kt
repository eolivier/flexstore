package org.flexstore.infra.spring.adapter.persistence.jpa.repository.twofactor

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.twofactor.TwoFactorChallengeRepository
import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.TwoFactorChallengeEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// The challenges are kept in the database, so that they survive a restart and are shared by several instances
@Repository
@Transactional
class PostgresTwoFactorChallengeRepositoryAdapter(private val jpaRepo: JpaTwoFactorChallengeRepository) :
    TwoFactorChallengeRepository {

    override fun save(challenge: TwoFactorChallenge) {
        jpaRepo.save(TwoFactorChallengeEntity(challenge.id.id.value, challenge.userId.value, challenge.issuedAt))
    }

    @Transactional(readOnly = true)
    override fun find(challengeId: TwoFactorChallengeId): TwoFactorChallengeLookup =
        jpaRepo.findById(challengeId.id.value)
            .map<TwoFactorChallengeLookup> { TwoFactorChallengeLookup.Found(it.toDomain()) }
            .orElse(TwoFactorChallengeLookup.NotFound)

    override fun delete(challengeId: TwoFactorChallengeId) {
        jpaRepo.deleteById(challengeId.id.value)
    }

    private fun TwoFactorChallengeEntity.toDomain() =
        TwoFactorChallenge(TwoFactorChallengeId(Identity(id)), UserId.ValidUserId(userId), issuedAt)
}
