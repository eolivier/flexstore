package org.flexstore.domain.user

// The status of the account of a user. A customer who creates their account waits for the verification of their
// email (PENDING_VERIFICATION) and can already sign in and order; an administrator can then change it
enum class AccountStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    SUSPENDED,
    LOCKED,
    DISABLED
}
