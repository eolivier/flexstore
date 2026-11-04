package org.flexstore.domain.user.model

import org.flexstore.domain.twofactor.model.TwoFactorChallengeId

// What a sign-in with the right password yields: a session right away, or, for an administrator who enabled
// two-factor authentication, a challenge that waits for the code of their authenticator app
sealed interface SignInOutcome {
    data class SignedIn(val user: User.DefinedUser) : SignInOutcome
    data class TwoFactorChallengeRequired(val challengeId: TwoFactorChallengeId) : SignInOutcome
}
