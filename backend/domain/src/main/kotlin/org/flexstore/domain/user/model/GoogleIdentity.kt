package org.flexstore.domain.user.model

import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException

// The token that Google gives to the browser of a person who signed in with their Google account
data class GoogleIdToken(val value: String)

// Who Google says a person is, once their token is verified. Google checked their email
data class GoogleIdentity(val fullName: FullName, val email: Email) {

    // A customer who signs in with Google for the first time gets an account. It is active at once, as Google
    // verified their email, and it has a password that nobody knows: they sign in with Google, or ask for a new
    // password
    fun customer(userId: UserId, password: PlainPassword) = User.DefinedUser(
        id = userId,
        fullName = fullName,
        email = email,
        password = password,
        role = Role.USER,
        status = AccountStatus.ACTIVE
    )
}

// The token is not Google's, is not meant for this shop, has expired, or its email is not verified; or signing
// in with Google is not set up
data class InvalidGoogleIdTokenException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
