package org.flexstore.domain.user.model

// The failed sign-ins in a row of an account (a wrong password): once the limit is reached, the account is
// locked and the count starts again; an administrator reactivates the account. A successful sign-in starts the
// count again too
data class FailedSignInCount(val value: Int) {

    fun plusOne() = FailedSignInCount(value + 1)

    fun hasReachedLimit() = value >= LIMIT

    fun isNone() = this == none()

    companion object {
        const val LIMIT = 5

        fun none() = FailedSignInCount(0)
    }
}
