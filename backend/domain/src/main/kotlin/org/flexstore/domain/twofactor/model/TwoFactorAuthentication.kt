package org.flexstore.domain.twofactor.model

// Whether the two-factor authentication (TOTP) of a user is set up: disabled, waiting for the first code of
// an enrollment that is not active yet, or enabled and checked at every sign-in
sealed interface TwoFactorAuthentication {

    fun isEnabled(): Boolean

    data object Disabled : TwoFactorAuthentication {
        override fun isEnabled() = false
    }

    data class PendingConfirmation(val secret: TwoFactorSecret) : TwoFactorAuthentication {
        override fun isEnabled() = false
    }

    data class Enabled(val secret: TwoFactorSecret) : TwoFactorAuthentication {
        override fun isEnabled() = true
    }
}
