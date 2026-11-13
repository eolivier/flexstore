package org.flexstore.domain.twofactor.model

import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.IdentityBased
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.user.model.UserId
import java.time.Duration
import java.time.Instant

data class TwoFactorChallengeId(override val value: Identity) : IdentityBased {
    companion object {
        fun nextId() = TwoFactorChallengeId(Identity.nextId())
    }
}

// A sign-in whose password was right, waiting for the second factor: a short-lived reference to the account that
// is kept until the code is verified, or dropped once it expires
data class TwoFactorChallenge(val id: TwoFactorChallengeId, val userId: UserId, val issuedAt: Instant) {

    fun isExpiredAt(instant: Instant) = Duration.between(issuedAt, instant) > VALIDITY

    companion object {
        val VALIDITY: Duration = Duration.ofMinutes(5)

        fun issuedFor(userId: UserId): TwoFactorChallenge {
            return TwoFactorChallenge(TwoFactorChallengeId.nextId(), userId, InstantProvider.now())
        }
    }
}

// The challenge found for a reference, or none: a wrong, already used or expired reference
sealed interface TwoFactorChallengeLookup {
    data class Found(val challenge: TwoFactorChallenge) : TwoFactorChallengeLookup
    data object NotFound : TwoFactorChallengeLookup
}
