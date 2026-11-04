package org.flexstore.domain.twofactor

import org.flexstore.domain.twofactor.model.TwoFactorChallenge
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorChallengeLookup

interface TwoFactorChallengeRepository {
    fun save(challenge: TwoFactorChallenge)
    fun find(challengeId: TwoFactorChallengeId): TwoFactorChallengeLookup
    fun delete(challengeId: TwoFactorChallengeId)
}
