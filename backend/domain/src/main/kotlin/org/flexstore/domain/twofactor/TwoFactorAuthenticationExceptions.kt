package org.flexstore.domain.twofactor

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

data class TwoFactorAuthenticationForbiddenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)

data class TwoFactorAuthenticationAlreadyEnabledException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)

data class TwoFactorAuthenticationNotEnabledException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)

data class TwoFactorEnrollmentNotPendingException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)

// The code typed by the administrator, at enrollment or at sign-in, does not match the current or adjacent one
data class TwoFactorCodeInvalidException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)

// The reference of the sign-in challenge is unknown, already used, or older than its validity
data class TwoFactorChallengeNotFoundException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
