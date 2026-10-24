package org.flexstore.domain.user.model

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// The status of the account of a user. A customer who creates their account waits for the verification of their
// email (PENDING_VERIFICATION) and can already sign in and order; an administrator can then change it
enum class AccountStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    LOCKED,
    DISABLED;

    // A suspended, locked or disabled account can neither sign in nor order
    fun allowsSignIn() = this == PENDING_VERIFICATION || this == ACTIVE

    fun refusesSignIn() = !allowsSignIn()

    fun allowsOrdering() = allowsSignIn()

    fun refusesOrdering() = !allowsOrdering()

    // An administrator activates, suspends, locks or disables an account; only the customer verifies their email
    fun canBeChosenByAnAdministrator() = this != PENDING_VERIFICATION

    fun cannotBeChosenByAnAdministrator() = !canBeChosenByAnAdministrator()
}

data class AccountSignInRefusedException(val status: AccountStatus, override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvalidStatusChangeException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
